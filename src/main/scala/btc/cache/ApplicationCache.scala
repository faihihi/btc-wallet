package btc.cache

import btc.config.CacheSettings
import cats.data.State
import com.github.blemale.scaffeine.AsyncLoadingCache
import com.github.blemale.scaffeine.Scaffeine

import java.util.concurrent.TimeUnit
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

trait ApplicationCache {

  val cacheSettings: CacheSettings

  def buildCacheAsync[Key, Value](loader: Key => Future[Value]): AsyncLoadingCache[Key, Value] = {
    val (inMemoryCache, _) = (for {
      _ <- setRefreshAfterWrite(cacheSettings)
    } yield ()).run(Scaffeine()).value

    inMemoryCache.buildAsyncFuture[Key, Value](loader)
  }

  private def setRefreshAfterWrite(cacheSettings: CacheSettings): State[Scaffeine[Any, Any], Unit] =
    State { cache =>
      val finiteDuration = FiniteDuration(cacheSettings.cacheExpiry.toNanos, TimeUnit.NANOSECONDS)
      (cache.refreshAfterWrite(finiteDuration), ())
    }
}
