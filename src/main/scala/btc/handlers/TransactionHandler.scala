package btc.handlers

import btc.db.DBRepositories
import btc.model.BTCTransaction
import btc.model.GetHistoriesRequest
import btc.model.GetHistoriesResponse
import btc.model.SaveTransactionResponse
import btc.model.TransactionError
import btc.queue.producer.TransactionProducer
import btc.validators.RequestValidators
import cats.data.EitherT
import cats.implicits._
import org.apache.kafka.clients.producer.RecordMetadata

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class TransactionHandler(
    dbRepositories: DBRepositories,
    requestValidators: RequestValidators,
    producer: TransactionProducer
)(implicit val ec: ExecutionContext) {

  def saveTransaction(transaction: BTCTransaction): Future[SaveTransactionResponse] = {
    val result: EitherT[Future, TransactionError, RecordMetadata] = for {
      request        <- EitherT.fromEither[Future](requestValidators.validateSaveTransactionRequest(transaction))
      recordMetadata <- EitherT.liftF(producer.produceToKafka(request)).leftMap { ex: Throwable =>
                          TransactionError(s"Unable to produce message to Kafka: ${ex.getMessage}")
                        }
    } yield recordMetadata

    result.value.map {
      case Right(_)  =>
        SaveTransactionResponse(
          success = true,
          message = s"Transaction at ${transaction.datetime} is saved.",
          error = None
        )
      case Left(err) =>
        SaveTransactionResponse(
          success = false,
          message = "Unable to save the transaction",
          error = Some(err.message)
        )
    }
  }

  def getTransactionHistories(request: GetHistoriesRequest): Future[GetHistoriesResponse] = {
    val result = for {
      req          <- EitherT.fromEither[Future](requestValidators.validateGetHistoriesRequest(request))
      transactions <-
        EitherT.fromEither[Future](dbRepositories.getTransactionHistories(req.startDateTime, req.endDateTime))
    } yield transactions

    result.value.map {
      case Right(transactions) => GetHistoriesResponse(transactions = transactions, error = None)
      case Left(err)           => GetHistoriesResponse(transactions = Seq.empty, error = Some(err.message))
    }
  }
}
