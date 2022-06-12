package btc

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import btc.config.ApplicationConfig
import btc.db.DBConnector
import btc.db.DBRepositories
import btc.handlers.TransactionHandler
import btc.queue.consumer.ConsumerSettingsBuilder
import btc.queue.consumer.TransactionConsumer
import btc.queue.producer.ProducerSettingsBuilder
import btc.queue.producer.TransactionProducer
import btc.validators.RequestValidators
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.Failure
import scala.util.Success

object Boot extends LazyLogging {

  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    import system.executionContext

    val futureBinding = Http().newServerAt("localhost", 8080).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info(
          "Server online at http://{}:{}/",
          address.getHostString,
          address.getPort
        )
      case Failure(ex)      =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  def main(args: Array[String]): Unit = {
    val config    = ConfigFactory.load()
    val appConfig = ApplicationConfig.apply(config.getConfig("btc-wallet"))

    /* Initiate Cassandra DB */
    val dbConnector    = new DBConnector(appConfig.dbSettings)
    val session        = dbConnector.connect() match {
      case Right(connectedSession) => connectedSession
      case Left(err)               =>
        logger.error(s"Unable to initialize DB: ${err.message}")
        throw new Exception(err.message)
    }
    val dbRepositories = new DBRepositories(session, appConfig.dbSettings)

    /* Initiate Kafka producer and consumer*/
    implicit val kafkaSystem = akka.actor.ActorSystem("BTCTransactionKafka")
    val kafkaSettings        = appConfig.kafkaSettings

    /* Initiate Kafka producer */
    val producerSettings    = new ProducerSettingsBuilder(kafkaSystem, kafkaSettings).build()
    val kafkaProducer       = producerSettings.createKafkaProducer() // TODO: change to async
    val transactionProducer = new TransactionProducer(kafkaProducer, kafkaSettings)

    // TODO: create kafka topic if not exist

    /* Initiate Kafka consumer */
    val consumerSettings = new ConsumerSettingsBuilder(kafkaSystem, kafkaSettings).build()
    val consumer         = new TransactionConsumer(consumerSettings, kafkaSettings, dbRepositories)
    /* Kafka consumer start listening */
    consumer.consume()

    val requestValidators  = new RequestValidators()
    val transactionHandler = new TransactionHandler(dbRepositories, requestValidators, transactionProducer)

    val rootBehavior = Behaviors.setup[Nothing] { context =>
      val routes =
        new Routes(transactionHandler, appConfig.httpSettings)(context.system)
      startHttpServer(routes.routes)(context.system)

      Behaviors.empty
    }

    val system = ActorSystem[Nothing](rootBehavior, "BTCWalletAPI")

    sys.addShutdownHook {
      logger.info(s"${system.name} service shutting down ")
      dbConnector.close(session)
      kafkaProducer.close()
    }
  }
}
