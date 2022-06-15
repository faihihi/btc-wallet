package btc

import btc.model.BTCTransaction
import btc.model.GetHistoriesRequest
import btc.model.GetHistoriesResponse
import btc.model.SaveTransactionResponse
import btc.model.TransactionMetadata

trait TestData {
  val defaultDateTimeStr         = "2019-10-05T14:45:11+07:00"
  val defaultJodaDateTime        = DateTimeUtils.parseToUTCDateTime(defaultDateTimeStr)
  val defaultAmount              = 13.355
  val defaultBTCTransaction      = BTCTransaction(defaultDateTimeStr, defaultAmount)
  val defaultTransactionMetadata = TransactionMetadata(defaultJodaDateTime, defaultAmount, "", 1)

  val saveTransactionRequestJson  =
    s"""
      |{
      |    "datetime": "$defaultDateTimeStr",
      |    "amount": $defaultAmount
      |}
      |""".stripMargin
  val saveTransactionRequest      = defaultBTCTransaction
  val saveTransactionResponseJson =
    "{\"message\":\"Transaction at 2019-10-05T14:45:11+07:00 is saved.\",\"success\":true}"
  val saveTransactionResponse     = SaveTransactionResponse(
    success = true,
    message = s"Transaction at $defaultDateTimeStr is saved.",
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
    "{\"transactions\":[{\"amount\":13.355,\"datetime\":\"2019-10-05T07:45:11+0000\"}]}"
  val getHistoriesResponse     = GetHistoriesResponse(
    transactions = Seq(BTCTransaction("2019-10-05T07:45:11+0000", defaultAmount)),
    error = None
  )
}
