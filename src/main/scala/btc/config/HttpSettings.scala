package btc.config

import akka.util.Timeout
import com.typesafe.config.Config

final case class HttpSettings(timeout: Timeout)
object HttpSettings {
  def apply(config: Config): HttpSettings = HttpSettings(timeout = Timeout.create(config.getDuration("timeout")))
}
