package btc.validators

import btc.DateTimeUtils.parseToUTCDateTime
import btc.model.BTCTransaction
import btc.model.GetHistoriesRequest
import btc.model.TransactionError
import org.joda.time.format._

class RequestValidators {

  def validateSaveTransactionRequest(
      request: BTCTransaction
  ): Either[TransactionError, BTCTransaction] = {
    for {
      _ <- Either.cond(
             request.datetime.nonEmpty,
             request,
             TransactionError(emptyErrorMsg("datetime"))
           )
      _ <- Either.cond(
             validateDateTime(request.datetime),
             request,
             TransactionError(formatErrorMsg("datetime"))
           )
    } yield request
  }

  def validateGetHistoriesRequest(
      request: GetHistoriesRequest
  ): Either[TransactionError, GetHistoriesRequest] = {
    for {
      _ <- Either.cond(
             request.startDateTime.nonEmpty && request.endDateTime.nonEmpty,
             request,
             TransactionError(emptyErrorMsg("startDateTime or endDateTime"))
           )
      _ <- Either.cond(
             validateDateTime(request.startDateTime) && validateDateTime(request.endDateTime),
             request,
             TransactionError(formatErrorMsg("startDateTime or endDateTime"))
           )
      _ <- Either.cond(
             startIsBeforeEnd(start = request.startDateTime, end = request.endDateTime),
             request,
             TransactionError(
               s"Request Validation Error: startDateTime is after endDateTime"
             )
           )
    } yield request
  }

  private def validateDateTime(dateTime: String): Boolean = parseDateTime(dateTime).nonEmpty

  private def startIsBeforeEnd(start: String, end: String): Boolean =
    (parseDateTime(start), parseDateTime(end)) match {
      case (Some(s), Some(e)) => s.isBefore(e)
      case (_, _)             => false
    }

  private def parseDateTime(dateTime: String)           =
    try { Some(parseToUTCDateTime(dateTime)) }
    catch { case _: IllegalArgumentException => None }

  private def emptyErrorMsg(fieldName: String): String  = s"Request Validation Error: $fieldName is empty"
  private def formatErrorMsg(fieldName: String): String =
    s"Request Validation Error: $fieldName is in the wrong format, please use this format yyyy-MM-dd'T'HH:mm:ssZ (ex. 2019-10-05T14:45:11+07:00)"

}
