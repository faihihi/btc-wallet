package btc

import com.datastax.driver.core.LocalDate
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat

import java.util.Date

object DateTimeUtils {
  val dateFormat     = "yyyy-MM-dd"
  val dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ssZ"

  val formatter     = DateTimeFormat.forPattern(dateTimeFormat)
  val dateFormatter = DateTimeFormat.forPattern(dateFormat)

  def parseToUTCDateTime(dateTime: String): DateTime = formatter.parseDateTime(dateTime).withZone(DateTimeZone.UTC)
  def parseToJodaDateTime(date: Date): DateTime      = new DateTime(date)

  def toDateTimeFormat(dateTime: DateTime): String = formatter.print(dateTime)
  def toDateFormat(dateTime: DateTime): String     = dateFormatter.print(dateTime)
  def toHourFormat(dateTime: DateTime): Int        = dateTime.getHourOfDay
}
