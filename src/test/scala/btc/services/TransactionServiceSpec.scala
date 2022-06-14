package btc.services

import btc.TestData
import btc.db.DBRepositories
import btc.model.BTCTransaction
import btc.model.GetHistoriesRequest
import btc.model.GetHistoriesResponse
import btc.model.SaveTransactionResponse
import btc.model.TransactionError
import btc.queue.producer.TransactionProducer
import btc.services.TransactionService
import btc.validators.RequestValidators
import com.datastax.driver.core.ResultSet
import org.mockito.scalatest.AsyncMockitoSugar
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import scala.concurrent.ExecutionContext

class TransactionServiceSpec extends AsyncWordSpec with Matchers with AsyncMockitoSugar with TestData {

  implicit val ex         = ExecutionContext.global
  val mockValidators      = mock[RequestValidators]
  val mockProducer        = mock[TransactionProducer]
  val mockMetadataService = mock[MetadataService]
  val transactionHandler  = new TransactionService(mockValidators, mockProducer, mockMetadataService)

  val mockResultSet = mock[ResultSet]

  "saveTransaction" should {
    "return response correctly" in {
      reset(mockValidators)
      when(mockValidators.validateSaveTransactionRequest(any[BTCTransaction]))
        .thenReturn(Right(saveTransactionRequest))

      transactionHandler.saveTransaction(saveTransactionRequest).map { response =>
        {
          verify(mockValidators, times(1))
            .validateSaveTransactionRequest(any[BTCTransaction])
          response.success shouldBe true
          response shouldBe saveTransactionResponse
        }
      }
    }

    "return error response when db throws error" in {
      reset(mockValidators)
      when(mockValidators.validateSaveTransactionRequest(any[BTCTransaction]))
        .thenReturn(Right(saveTransactionRequest))

      val expectedResponse = SaveTransactionResponse(
        success = false,
        message = "Unable to save the transaction",
        error = Some("some error")
      )
      transactionHandler.saveTransaction(saveTransactionRequest).map { response =>
        {
          verify(mockValidators, times(1))
            .validateSaveTransactionRequest(any[BTCTransaction])
          response shouldBe expectedResponse
        }
      }
    }

    "return error response when validator throws error" in {
      reset(mockValidators)
      when(mockValidators.validateSaveTransactionRequest(any[BTCTransaction]))
        .thenReturn(Left(TransactionError("some validation error")))

      val expectedResponse = SaveTransactionResponse(
        success = false,
        message = "Unable to save the transaction",
        error = Some("some validation error")
      )
      transactionHandler.saveTransaction(saveTransactionRequest).map { response =>
        {
          verify(mockValidators, times(1))
            .validateSaveTransactionRequest(any[BTCTransaction])
          response shouldBe expectedResponse
        }
      }
    }
  }

  "getTransactionHistories" should {
    "return response correctly" in {
      reset(mockValidators)
      when(mockValidators.validateGetHistoriesRequest(any[GetHistoriesRequest]))
        .thenReturn(Right(getHistoriesRequest))

      transactionHandler.getTransactionHistories(getHistoriesRequest).map { response =>
        {
          verify(mockValidators, times(1))
            .validateGetHistoriesRequest(any[GetHistoriesRequest])
          response shouldBe getHistoriesResponse
        }
      }
    }

    "return error response when db throws error" in {
      reset(mockValidators)
      when(mockValidators.validateGetHistoriesRequest(any[GetHistoriesRequest]))
        .thenReturn(Right(getHistoriesRequest))

      val expectedResponse =
        GetHistoriesResponse(Seq.empty, Some("some DB error"))
      transactionHandler.getTransactionHistories(getHistoriesRequest).map { response =>
        {
          verify(mockValidators, times(1))
            .validateGetHistoriesRequest(any[GetHistoriesRequest])
          response shouldBe expectedResponse
        }
      }
    }

    "return error response when validator throws error" in {
      reset(mockValidators)
      when(mockValidators.validateGetHistoriesRequest(any[GetHistoriesRequest]))
        .thenReturn(Left(TransactionError("some validation error")))

      val expectedResponse =
        GetHistoriesResponse(Seq.empty, Some("some validation error"))
      transactionHandler.getTransactionHistories(getHistoriesRequest).map { response =>
        {
          verify(mockValidators, times(1))
            .validateGetHistoriesRequest(any[GetHistoriesRequest])
          response shouldBe expectedResponse
        }
      }
    }
  }

}
