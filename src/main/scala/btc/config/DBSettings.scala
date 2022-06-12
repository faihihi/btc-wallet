package btc.config

import com.typesafe.config.Config

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
