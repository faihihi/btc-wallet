package btc.queue

import btc.config.KafkaSettings
import btc.db.DBRepositories
import btc.queue.consumer.ConsumerSettingsBuilder
import btc.queue.consumer.TransactionConsumer
import btc.queue.producer.ProducerSettingsBuilder
import org.apache.kafka.clients.admin.Admin
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.Producer

import scala.jdk.CollectionConverters._
import java.util.Properties
import scala.concurrent.ExecutionContext

class KafkaProvider(dbRepositories: DBRepositories, kafkaSettings: KafkaSettings)(implicit val ec: ExecutionContext) {

  def initiateAndBuildProvider(): Producer[String, String] = {
    /* Initiate Kafka producer and consumer*/
    implicit val kafkaSystem = akka.actor.ActorSystem("BTCTransactionKafka")

    val props       = new Properties()
    props.put("bootstrap.servers", kafkaSettings.brokers.mkString(","))
    val kafkaClient = Admin.create(props)
    val kafkaTopic  = new NewTopic(kafkaSettings.topic, 3, 1.toShort)
    kafkaClient.createTopics(List(kafkaTopic).asJava)

    /* Initiate Kafka consumer */
    val consumerSettings = new ConsumerSettingsBuilder(kafkaSystem, kafkaSettings).build()
    val consumer         = new TransactionConsumer(consumerSettings, kafkaSettings, dbRepositories)
    /* Kafka consumer start listening */
    consumer.consume()

    /* Initiate Kafka producer */
    val producerSettings = new ProducerSettingsBuilder(kafkaSystem, kafkaSettings).build()
    producerSettings.createKafkaProducer() // TODO: change to async
  }

}
