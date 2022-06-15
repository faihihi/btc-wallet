package btc.services

import btc.DateTimeUtils
import btc.cache.ApplicationCache
import btc.config.CacheSettings
import btc.db.DBRepositories
import btc.model.TransactionError
import btc.model.TransactionMetadata
import btc.services.MetadataService.transactionCacheBySingleDate
import btc.services.MetadataService.TransactionCacheByPeriod
import com.github.blemale.scaffeine.AsyncLoadingCache
import com.typesafe.scalalogging.LazyLogging
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import scala.util.Try

class MetadataService(dbRepositories: DBRepositories, val cacheSettings: CacheSettings)(implicit
    val ec: ExecutionContext
) extends ApplicationCache
    with LazyLogging {

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

  def getByPeriod(start: DateTime, end: DateTime): Future[Either[TransactionError, Seq[TransactionMetadata]]] = {
    val startDate = DateTimeUtils.toDateFormat(start)
    val startHour = DateTimeUtils.toHourFormat(start)
    val endDate   = DateTimeUtils.toDateFormat(end)
    val endHour   = DateTimeUtils.toHourFormat(end)

    val resultF = if (startDate == endDate) {
      transactionCacheBySingleDate(startDate)
        .get(())
        .map { cache =>
          {
            // This may be overkill if the data load is not huge
            val cachedByHour = cache.filter(byHr => byHr._1 >= startHour && byHr._1 <= endHour).flatMap(_._2).toSeq
            cachedByHour.filter(t => t.dateTime.isAfter(start) && t.dateTime.isBefore(end))
          }
        }
    } else {
      transactionCacheByPeriod(DateTimeUtils.toDateTimeFormat(start), DateTimeUtils.toDateTimeFormat(end))
        .get(())
        .map { cache => cache.flatMap(_._2).toSeq }
    }

    for {
      result <- resultF
    } yield {
      Try { result } match {
        case Success(value)     => Right(value)
        case Failure(exception) =>
          val errMsg = s"Unable to get from cache: ${exception.getMessage}"
          logger.error(errMsg)
          Left(TransactionError(errMsg))
      }
    }
  }
}

object MetadataService {
  type transactionCacheBySingleDate = Map[Int, Seq[TransactionMetadata]]    // Hour as key
  type TransactionCacheByPeriod     = Map[String, Seq[TransactionMetadata]] // Date as key
}
