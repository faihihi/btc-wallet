package btc

import akka.util.Timeout
import com.typesafe.config.Config

final case class ApplicationConfig(
    httpSettings: HttpSettings,
    dbSettings: DBSettings
)

object ApplicationConfig {
  def apply(config: Config): ApplicationConfig =
    ApplicationConfig(
      httpSettings = HttpSettings.apply(config.getConfig("http")),
      dbSettings = DBSettings.apply(config.getConfig("db"))
    )
}

final case class HttpSettings(timeout: Timeout)

object HttpSettings {
  def apply(config: Config): HttpSettings =
    HttpSettings(timeout = Timeout.create(config.getDuration("timeout")))
}

final case class DBSettings(
    host: String,
    port: Int,
    keyspace: String,
    tableName: String
)

object DBSettings {
  def apply(config: Config): DBSettings =
    DBSettings(
      host = config.getString("host"),
      port = config.getInt("port"),
      keyspace = config.getString("keyspace"),
      tableName = config.getString("table-name")
    )
}
