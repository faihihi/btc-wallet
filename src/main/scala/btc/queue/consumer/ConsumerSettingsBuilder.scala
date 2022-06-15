package btc.queue.consumer

import akka.actor.ActorSystem
import akka.kafka.ConsumerSettings
import btc.config.KafkaSettings
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

class ConsumerSettingsBuilder(system: ActorSystem, kafkaSettings: KafkaSettings) {

  private val kafkaConsumerSettings = kafkaSettings.consumer

  def build(): ConsumerSettings[String, String] =
    ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withGroupId(kafkaConsumerSettings.groupId)
      .withBootstrapServers(kafkaSettings.brokers.mkString(","))
      .withProperties(
        Map(
          ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG      -> kafkaConsumerSettings.autoCommit.toString,
          ConsumerConfig.AUTO_OFFSET_RESET_CONFIG       -> kafkaConsumerSettings.autoOffsetReset,
          ConsumerConfig.MAX_POLL_RECORDS_CONFIG        -> kafkaConsumerSettings.maxPollRecord.toString,
          CommonClientConfigs.SESSION_TIMEOUT_MS_CONFIG -> kafkaConsumerSettings.sessionTimeOut.toString
        )
      )
}
