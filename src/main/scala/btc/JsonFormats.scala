package btc

import btc.model.{
  BTCTransaction,
  GetHistoriesRequest,
  GetHistoriesResponse,
  SaveTransactionResponse,
  TransactionError
}
import spray.json.DefaultJsonProtocol

object JsonFormats {
  import DefaultJsonProtocol._

  implicit val BTCTransactionJsonFormat = jsonFormat2(BTCTransaction)
  implicit val GetHistoriesRequestJsonFormat = jsonFormat2(GetHistoriesRequest)
  implicit val GetHistoriesResponseJsonFormat = jsonFormat2(
    GetHistoriesResponse
  )
  implicit val SaveTransactionResponseJsonFormat = jsonFormat3(
    SaveTransactionResponse
  )
}
