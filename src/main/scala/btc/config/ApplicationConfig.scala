package btc.config

import com.typesafe.config.Config

final case class ApplicationConfig(
    httpSettings: HttpSettings,
    dbSettings: DBSettings,
    kafkaSettings: KafkaSettings
)

object ApplicationConfig {
  def apply(config: Config): ApplicationConfig =
    ApplicationConfig(
      httpSettings = HttpSettings.apply(config.getConfig("http")),
      dbSettings = DBSettings.apply(config.getConfig("db")),
      kafkaSettings = KafkaSettings.apply(config.getConfig("kafka"))
    )
}
