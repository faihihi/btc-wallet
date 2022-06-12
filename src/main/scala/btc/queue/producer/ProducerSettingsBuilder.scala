package btc.queue.producer

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import btc.config.KafkaSettings
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer

class ProducerSettingsBuilder(system: ActorSystem, kafkaSettings: KafkaSettings) {

  private val kafkaProducerSettings = kafkaSettings.producer

  def build(): ProducerSettings[String, String] =
    ProducerSettings(system, new StringSerializer, new StringSerializer)
      .withBootstrapServers(kafkaSettings.brokers.mkString(","))
      .withProperties(
        Map(
          ProducerConfig.BATCH_SIZE_CONFIG         -> kafkaProducerSettings.batchSize.toString,
          ProducerConfig.LINGER_MS_CONFIG          -> kafkaProducerSettings.linger.toMillis.toString,
          ProducerConfig.ACKS_CONFIG               -> kafkaProducerSettings.acks.toString,
          ProducerConfig.RETRIES_CONFIG            -> kafkaProducerSettings.retries.toString,
          ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG -> kafkaProducerSettings.requestTimeOut.toMillis.toString
        )
      )
}
