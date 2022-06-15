package btc.services

import btc.TestData
import btc.model.BTCTransaction
import btc.model.GetHistoriesRequest
import btc.model.GetHistoriesResponse
import btc.model.SaveTransactionResponse
import btc.model.TransactionError
import btc.model.TransactionMetadata
import btc.queue.producer.TransactionProducer
import btc.validators.RequestValidators
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import org.joda.time.DateTime
import org.mockito.scalatest.AsyncMockitoSugar
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class TransactionServiceSpec extends AsyncWordSpec with Matchers with AsyncMockitoSugar with TestData {

  implicit val ex         = ExecutionContext.global
  val mockValidators      = mock[RequestValidators]
  val mockProducer        = mock[TransactionProducer]
  val mockMetadataService = mock[MetadataService]
  val transactionHandler  = new TransactionService(mockValidators, mockProducer, mockMetadataService)

  private val mockRecordMetadata = new RecordMetadata(new TopicPartition("", 1), 1, 1, 1, 1, 1, 1)

  "saveTransaction" should {
    "return response correctly" in {
      reset(mockValidators, mockProducer)
      when(mockValidators.validateSaveTransactionRequest(any[BTCTransaction]))
        .thenReturn(Right(saveTransactionRequest))
      when(mockProducer.produceToKafka(any[TransactionMetadata]))
        .thenReturn(Right(mockRecordMetadata))

      transactionHandler.saveTransaction(saveTransactionRequest).map { response =>
        {
          verify(mockValidators, times(1)).validateSaveTransactionRequest(any[BTCTransaction])
          verify(mockProducer, times(1)).produceToKafka(any[TransactionMetadata])
          response.success shouldBe true
          response shouldBe saveTransactionResponse
        }
      }
    }

    "return error response when producer throws error" in {
      reset(mockValidators, mockProducer)
      when(mockValidators.validateSaveTransactionRequest(any[BTCTransaction]))
        .thenReturn(Right(saveTransactionRequest))
      when(mockProducer.produceToKafka(any[TransactionMetadata]))
        .thenReturn(Left(TransactionError("Failed to deserialize")))

      val expectedResponse = SaveTransactionResponse(
        success = false,
        message = "Unable to save the transaction",
        error = Some("Failed to deserialize")
      )
      transactionHandler.saveTransaction(saveTransactionRequest).map { response =>
        {
          verify(mockValidators, times(1)).validateSaveTransactionRequest(any[BTCTransaction])
          verify(mockProducer, times(1)).produceToKafka(any[TransactionMetadata])
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
          verify(mockValidators, times(1)).validateSaveTransactionRequest(any[BTCTransaction])
          response shouldBe expectedResponse
        }
      }
    }
  }

  "getTransactionHistories" should {
    "return response correctly" in {
      reset(mockValidators, mockMetadataService)
      when(mockValidators.validateGetHistoriesRequest(any[GetHistoriesRequest]))
        .thenReturn(Right(getHistoriesRequest))
      when(mockMetadataService.getByPeriod(any[DateTime], any[DateTime]))
        .thenReturn(Future.successful(Right(Seq(defaultTransactionMetadata))))

      transactionHandler.getTransactionHistories(getHistoriesRequest).map { response =>
        {
          verify(mockValidators, times(1)).validateGetHistoriesRequest(any[GetHistoriesRequest])
          verify(mockMetadataService, times(1)).getByPeriod(any[DateTime], any[DateTime])
          response shouldBe getHistoriesResponse
        }
      }
    }

    "return error response when metadata service throws error" in {
      reset(mockValidators, mockMetadataService)
      when(mockValidators.validateGetHistoriesRequest(any[GetHistoriesRequest]))
        .thenReturn(Right(getHistoriesRequest))
      when(mockMetadataService.getByPeriod(any[DateTime], any[DateTime]))
        .thenReturn(Future.successful(Left(TransactionError("some exception"))))

      val expectedResponse = GetHistoriesResponse(Seq.empty, Some("some exception"))
      transactionHandler.getTransactionHistories(getHistoriesRequest).map { response =>
        {
          verify(mockValidators, times(1)).validateGetHistoriesRequest(any[GetHistoriesRequest])
          verify(mockMetadataService, times(1)).getByPeriod(any[DateTime], any[DateTime])
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
