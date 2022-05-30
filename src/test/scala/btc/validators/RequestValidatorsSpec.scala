package btc.validators

import btc.TestData
import btc.model._
import org.mockito.scalatest.AsyncMockitoSugar
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class RequestValidatorsSpec
    extends AsyncWordSpec
    with Matchers
    with AsyncMockitoSugar
    with TestData {

  val requestValidators = new RequestValidators()

  "validateSaveTransactionRequest" should {
    "validate request" in {
      val result =
        requestValidators.validateSaveTransactionRequest(saveTransactionRequest)
      result.isRight shouldBe true
      result.right.get shouldBe saveTransactionRequest
    }

    "invalidate when datetime is empty" in {
      val req = saveTransactionRequest.copy(datetime = "")
      val result = requestValidators.validateSaveTransactionRequest(req)
      result.isLeft shouldBe true
      result.left.get shouldBe TransactionError(
        "Request Validation Error: datetime is empty"
      )
    }

    "invalidate when datetime format is invalid" in {
      val req = saveTransactionRequest.copy(datetime = "2019/10/05")
      val result = requestValidators.validateSaveTransactionRequest(req)
      result.isLeft shouldBe true
      result.left.get shouldBe TransactionError(
        "Request Validation Error: datetime is in the wrong format, please use this format yyyy-MM-dd'T'HH:mm:ssZ (ex. 2019-10-05T14:45:11+07:00)"
      )
    }
  }

  "validateGetHistoriesRequest" should {
    "validate request" in {
      val result =
        requestValidators.validateGetHistoriesRequest(getHistoriesRequest)
      result.isRight shouldBe true
      result.right.get shouldBe getHistoriesRequest
    }

    "invalidate when dateTime is empty" in {
      val req = getHistoriesRequest.copy(startDateTime = "")
      val result = requestValidators.validateGetHistoriesRequest(req)
      result.isLeft shouldBe true
      result.left.get shouldBe TransactionError(
        "Request Validation Error: startDateTime or endDateTime is empty"
      )
    }

    "invalidate when dateTime format is invalid" in {
      val req = getHistoriesRequest.copy(endDateTime = "2019/10/05")
      val result = requestValidators.validateGetHistoriesRequest(req)
      result.isLeft shouldBe true
      result.left.get shouldBe TransactionError(
        "Request Validation Error: startDateTime or endDateTime is in the wrong format, please use this format yyyy-MM-dd'T'HH:mm:ssZ (ex. 2019-10-05T14:45:11+07:00)"
      )
    }

    "invalidate when startDateTime is not before endDateTime" in {
      val req =
        getHistoriesRequest.copy(startDateTime = "2019-10-06T14:35:05+07:00")
      val result = requestValidators.validateGetHistoriesRequest(req)
      result.isLeft shouldBe true
      result.left.get shouldBe TransactionError(
        "Request Validation Error: startDateTime is after endDateTime"
      )
    }
  }
}
