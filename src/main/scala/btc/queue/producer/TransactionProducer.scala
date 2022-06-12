package btc.queue.producer

import btc.JsonFormats._
import btc.config.KafkaSettings
import btc.model.BTCTransaction
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.producer._
import spray.json.enrichAny

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class TransactionProducer(producer: Producer[String, String], kafkaSettings: KafkaSettings)(implicit
    val ec: ExecutionContext
) extends LazyLogging {

  def produceToKafka(transaction: BTCTransaction): Future[RecordMetadata] =
    Future {
      val serializedTransaction = transaction.toJson.toString
      val record                = new ProducerRecord(kafkaSettings.topic, transaction.datetime, serializedTransaction)
      producer.send(record).get
    }
}
