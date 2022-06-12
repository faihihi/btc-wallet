package btc

import btc.model.BTCTransaction
import btc.model.GetHistoriesRequest
import btc.model.GetHistoriesResponse
import btc.model.SaveTransactionResponse
import spray.json.DefaultJsonProtocol

object JsonFormats {
  import DefaultJsonProtocol._

  implicit val BTCTransactionJsonFormat          = jsonFormat2(BTCTransaction)
  implicit val GetHistoriesRequestJsonFormat     = jsonFormat2(GetHistoriesRequest)
  implicit val GetHistoriesResponseJsonFormat    = jsonFormat2(GetHistoriesResponse)
  implicit val SaveTransactionResponseJsonFormat = jsonFormat3(SaveTransactionResponse)
}
