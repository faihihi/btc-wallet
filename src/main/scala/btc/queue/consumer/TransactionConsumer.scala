package btc.queue.consumer

import akka.Done
import akka.kafka.ConsumerSettings
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import btc.config.KafkaSettings
import btc.db.DBRepositories
import btc.model.TransactionMetadata
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.consumer.ConsumerRecord
import spray.json._
import btc.JsonFormats._

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class TransactionConsumer(
    consumerSettings: ConsumerSettings[String, String],
    kafkaSettings: KafkaSettings,
    dbRepositories: DBRepositories
)(implicit val ec: ExecutionContext)
    extends LazyLogging {

  def consume()(implicit materializer: Materializer): Future[Unit] = {
    Consumer
      .plainSource(consumerSettings, Subscriptions.topics(kafkaSettings.topic))
      .mapAsync(kafkaSettings.consumer.parallelism)(processRecord)
      .runWith(Sink.ignore)
      .recover(error => logger.error(s"Kafka stream failed, stopping actor: ${error.getMessage}"))
      .map(_ => ())
  }

  private def processRecord(record: ConsumerRecord[String, String]): Future[Done] =
    Future {
      val transaction = record.value().parseJson.convertTo[TransactionMetadata]
      dbRepositories.insertTransaction(transaction) match {
        case Right(_)  => Done
        case Left(err) =>
          logger.error(s"Unable to insert to DB: ${err.message}") // TODO: retry??
          Done
      }
    }
}
