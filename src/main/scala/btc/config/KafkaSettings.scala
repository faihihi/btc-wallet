package btc.config

import com.typesafe.config.Config

import java.time.Duration
import scala.jdk.CollectionConverters.IterableHasAsScala

final case class KafkaSettings(
    brokers: List[String],
    topic: String,
    producer: KafkaProducerSettings,
    consumer: KafkaConsumerSettings
)

object KafkaSettings {
  def apply(config: Config): KafkaSettings =
    KafkaSettings(
      brokers = config.getStringList("brokers").asScala.toList,
      topic = config.getString("topic"),
      producer = KafkaProducerSettings.apply(config.getConfig("producer")),
      consumer = KafkaConsumerSettings.apply(config.getConfig("consumer"))
    )
}

final case class KafkaProducerSettings(
    retries: Int,
    batchSize: Long,
    requestTimeOut: Duration,
    linger: Duration,
    acks: Int
)
object KafkaProducerSettings {
  def apply(config: Config): KafkaProducerSettings =
    KafkaProducerSettings(
      retries = config.getInt("retries"),
      batchSize = config.getLong("batch-size"),
      requestTimeOut = config.getDuration("request-time-out"),
      linger = config.getDuration("linger"),
      acks = config.getInt("acks")
    )
}

final case class KafkaConsumerSettings(
    groupId: String,
    parallelism: Int,
    autoCommit: Boolean,
    autoOffsetReset: String,
    maxPollRecord: Int,
    sessionTimeOut: Long
)

object KafkaConsumerSettings {
  def apply(config: Config): KafkaConsumerSettings =
    KafkaConsumerSettings(
      groupId = config.getString("group-id"),
      parallelism = config.getInt("parallelism"),
      autoCommit = config.getBoolean("auto-commit"),
      autoOffsetReset = config.getString("auto-offset-reset"),
      maxPollRecord = config.getInt("max-poll-record"),
      sessionTimeOut = config.getLong("session-time-out")
    )
}
