package btc.queue.producer

import btc.DateTimeUtils
import btc.JsonFormats._
import btc.config.KafkaSettings
import btc.model.TransactionError
import btc.model.TransactionMetadata
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.producer._
import spray.json.enrichAny

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Try

class TransactionProducer(producer: Producer[String, String], kafkaSettings: KafkaSettings)(implicit
    val ec: ExecutionContext
) extends LazyLogging {

  def produceToKafka(transaction: TransactionMetadata): Either[TransactionError, RecordMetadata] =
    try {
      val serializedTransaction = transaction.toJson.toString
      val dateTimeStr           = DateTimeUtils.toDateTimeFormat(transaction.dateTime)
      val record                = new ProducerRecord(kafkaSettings.topic, dateTimeStr, serializedTransaction)
      Right(producer.send(record).get)
    } catch { case ex: Throwable => Left(TransactionError(s"Unable to produce to Kafka: ${ex.getMessage}")) }
}
