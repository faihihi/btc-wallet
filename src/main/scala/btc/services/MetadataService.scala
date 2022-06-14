package btc.services

import btc.DateTimeUtils
import btc.cache.ApplicationCache
import btc.config.CacheSettings
import btc.db.DBRepositories
import btc.model.TransactionMetadata
import btc.services.MetadataService.transactionCacheBySingleDate
import btc.services.MetadataService.TransactionCacheByPeriod
import com.github.blemale.scaffeine.AsyncLoadingCache
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class MetadataService(dbRepositories: DBRepositories, val cacheSettings: CacheSettings)(implicit
    val ec: ExecutionContext
) extends ApplicationCache {

  private def transactionCacheBySingleDate(date: String): AsyncLoadingCache[Unit, transactionCacheBySingleDate] =
    buildCacheAsync[Unit, transactionCacheBySingleDate](_ => buildTransactionDateCache(date))

  private def transactionCacheByPeriod(start: String, end: String): AsyncLoadingCache[Unit, TransactionCacheByPeriod] =
    buildCacheAsync[Unit, TransactionCacheByPeriod](_ => buildTransactionPeriodCache(start, end))

  private def buildTransactionDateCache(date: String): Future[transactionCacheBySingleDate] =
    Future {
      dbRepositories
        .getTransactionByDate(date)
        .groupBy(_.hour)
        .map { case (key, value) =>
          key -> value
        }
    }

  private def buildTransactionPeriodCache(start: String, end: String): Future[TransactionCacheByPeriod] =
    Future {
      dbRepositories
        .getTransactionByPeriod(start, end)
        .groupBy(_.date)
        .map { case (key, value) => key -> value }
    }

  def getByPeriod(start: DateTime, end: DateTime): Future[Seq[TransactionMetadata]] = {
    val startDate = DateTimeUtils.toDateFormat(start)
    val startHour = DateTimeUtils.toHourFormat(start)
    val endDate   = DateTimeUtils.toDateFormat(end)
    val endHour   = DateTimeUtils.toHourFormat(end)

    if (startDate == endDate) {
      transactionCacheBySingleDate(startDate)
        .get(())
        .map { _.filter(byHr => byHr._1 >= startHour && byHr._1 <= endHour).flatMap(_._2).toSeq }
    } else {
      transactionCacheByPeriod(DateTimeUtils.toDateTimeFormat(start), DateTimeUtils.toDateTimeFormat(end))
        .get(())
        .map { cache => cache.flatMap(_._2).toSeq }
    }
  }

}

object MetadataService {
  type transactionCacheBySingleDate = Map[Int, Seq[TransactionMetadata]]    // Hour as key
  type TransactionCacheByPeriod     = Map[String, Seq[TransactionMetadata]] // Date as key
}
