package btc.handlers

import btc.db.DBRepositories
import btc.model.{
  BTCTransaction,
  GetHistoriesRequest,
  GetHistoriesResponse,
  SaveTransactionResponse
}
import btc.validators.RequestValidators
import cats.data.EitherT
import cats.implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TransactionHandler(
    dbRepositories: DBRepositories,
    requestValidators: RequestValidators
) {

  def saveTransaction(
      transaction: BTCTransaction
  ): Future[SaveTransactionResponse] = {
    val result = for {
      request <- EitherT.fromEither[Future](
        requestValidators.validateSaveTransactionRequest(transaction)
      )
      resultSet <- EitherT.fromEither[Future](
        dbRepositories.insertTransaction(request)
      )
    } yield resultSet

    result.value.map {
      case Right(_) =>
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

  def getTransactionHistories(
      request: GetHistoriesRequest
  ): Future[GetHistoriesResponse] = {
    val result = for {
      req <- EitherT.fromEither[Future](
        requestValidators.validateGetHistoriesRequest(request)
      )
      transactions <- EitherT.fromEither[Future](
        dbRepositories.getTransactionHistories(
          req.startDateTime,
          req.endDateTime
        )
      )
    } yield transactions

    result.value.map {
      case Right(transactions) =>
        GetHistoriesResponse(transactions = transactions, error = None)
      case Left(err) =>
        GetHistoriesResponse(
          transactions = Seq.empty,
          error = Some(err.message)
        )
    }
  }
}
