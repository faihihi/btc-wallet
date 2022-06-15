package btc.services

import btc.DateTimeUtils
import btc.DateTimeUtils._
import btc.model.BTCTransaction
import btc.model.GetHistoriesRequest
import btc.model.GetHistoriesResponse
import btc.model.SaveTransactionResponse
import btc.model.TransactionError
import btc.model.TransactionMetadata
import btc.queue.producer.TransactionProducer
import btc.validators.RequestValidators
import cats.data.EitherT
import cats.implicits._
import org.apache.kafka.clients.producer.RecordMetadata

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

class TransactionService(
    requestValidators: RequestValidators,
    producer: TransactionProducer,
    metadataService: MetadataService
)(implicit val ec: ExecutionContext) {

  def saveTransaction(transaction: BTCTransaction): Future[SaveTransactionResponse] = {
    val result: EitherT[Future, TransactionError, RecordMetadata] = for {
      request        <- EitherT.fromEither[Future](requestValidators.validateSaveTransactionRequest(transaction))
      metadata        = buildTransactionMetadata(request)
      recordMetadata <- EitherT.fromEither[Future](producer.produceToKafka(metadata))
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

  private def buildTransactionMetadata(transaction: BTCTransaction): TransactionMetadata = {
    val dateTimeUTC = parseToUTCDateTime(transaction.datetime)
    TransactionMetadata(
      dateTime = dateTimeUTC,
      amount = transaction.amount,
      date = toDateFormat(dateTimeUTC),
      hour = toHourFormat(dateTimeUTC)
    )
  }

  def getTransactionHistories(request: GetHistoriesRequest): Future[GetHistoriesResponse] = {
    val result: EitherT[Future, TransactionError, Seq[BTCTransaction]] = for {
      _            <- EitherT.fromEither[Future](requestValidators.validateGetHistoriesRequest(request))
      startUTC      = DateTimeUtils.parseToUTCDateTime(request.startDateTime)
      endUTC        = DateTimeUtils.parseToUTCDateTime(request.endDateTime)
      transactions <- EitherT(metadataService.getByPeriod(startUTC, endUTC))
    } yield transactions.map(transaction => {
      BTCTransaction(
        datetime = DateTimeUtils.toDateTimeFormat(transaction.dateTime),
        amount = transaction.amount
      )
    })

    result.value.map {
      case Right(transactions) => GetHistoriesResponse(transactions = transactions, error = None)
      case Left(err)           => GetHistoriesResponse(transactions = Seq.empty, error = Some(err.message))
    }
  }
}
