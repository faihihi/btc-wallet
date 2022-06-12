package btc

import btc.model.BTCTransaction
import btc.model.GetHistoriesRequest
import btc.model.GetHistoriesResponse
import btc.model.SaveTransactionResponse

trait TestData {
  val defaultBTCTransaction =
    BTCTransaction("2019-10-05T14:45:11+07:00", 13.355)

  val saveTransactionRequestJson  =
    """
      |{
      |    "datetime": "2019-10-05T14:45:11+07:00",
      |    "amount": 13.355
      |}
      |""".stripMargin
  val saveTransactionRequest      = defaultBTCTransaction
  val saveTransactionResponseJson =
    "{\"message\":\"Transaction at 2019-10-05T14:45:11+07:00 is saved.\",\"success\":true}"
  val saveTransactionResponse     = SaveTransactionResponse(
    success = true,
    message = "Transaction at 2019-10-05T14:45:11+07:00 is saved.",
    error = None
  )

  val getHistoriesRequestJson  =
    """
      |{
      |    "startDateTime": "2019-10-05T14:35:05+07:00",
      |    "endDateTime": "2019-10-05T15:58:05+07:00"
      |}
      |""".stripMargin
  val getHistoriesRequest      = GetHistoriesRequest(
    startDateTime = "2019-10-05T14:35:05+07:00",
    endDateTime = "2019-10-05T15:58:05+07:00"
  )
  val getHistoriesResponseJson =
    "{\"transactions\":[{\"amount\":13.355,\"datetime\":\"2019-10-05T14:45:11+07:00\"}]}"
  val getHistoriesResponse     = GetHistoriesResponse(
    transactions = Seq(BTCTransaction("2019-10-05T14:45:11+07:00", 13.355)),
    error = None
  )
}
