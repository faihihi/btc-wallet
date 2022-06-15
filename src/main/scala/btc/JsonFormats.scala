package btc

import btc.model.BTCTransaction
import btc.model.GetHistoriesRequest
import btc.model.GetHistoriesResponse
import btc.model.SaveTransactionResponse
import btc.model.TransactionMetadata
import org.joda.time.DateTime
import spray.json.DefaultJsonProtocol
import spray.json.DeserializationException
import spray.json.JsString
import spray.json.JsValue
import spray.json.RootJsonFormat

object JsonFormats extends DefaultJsonProtocol {

  implicit object VideoStorageErrorTypeJsonFormat extends RootJsonFormat[DateTime] {
    def write(obj: DateTime): JsValue      = JsString(DateTimeUtils.toDateTimeFormat(obj))
    def read(jsonValue: JsValue): DateTime =
      jsonValue match {
        case JsString(stringObj) => DateTimeUtils.parseToUTCDateTime(stringObj)
        case otherValue          =>
          throw DeserializationException(s" Unable to deserialize dateTime ${otherValue.toString}")
      }
  }

  implicit val BTCTransactionJsonFormat          = jsonFormat2(BTCTransaction)
  implicit val TransactionMetadataJsonFormat     = jsonFormat4(TransactionMetadata)
  implicit val GetHistoriesRequestJsonFormat     = jsonFormat2(GetHistoriesRequest)
  implicit val GetHistoriesResponseJsonFormat    = jsonFormat2(GetHistoriesResponse)
  implicit val SaveTransactionResponseJsonFormat = jsonFormat3(SaveTransactionResponse)
}
