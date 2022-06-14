package btc.config

import java.time.Duration
import com.typesafe.config.Config

final case class CacheSettings(cacheExpiry: Duration)

object CacheSettings {
  def apply(config: Config): CacheSettings = CacheSettings(config.getDuration("cache-expiry"))
}
