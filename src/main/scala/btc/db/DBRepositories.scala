package btc.db

import btc.DateTimeUtils
import btc.config.DBSettings
import btc.db.DBConstants._
import btc.model.TransactionError
import btc.model.TransactionMetadata
import com.datastax.driver.core.ResultSet
import com.datastax.driver.core.Session
import com.typesafe.scalalogging.LazyLogging

import scala.collection.convert.ImplicitConversions.`iterator asScala`
import scala.util.Try

class DBRepositories(session: Session, dbSetting: DBSettings) extends LazyLogging {

  val tableName = dbSetting.tableName

  def insertTransaction(
      transaction: TransactionMetadata
  ): Either[TransactionError, ResultSet] =
    Try {
      val dateTime = DateTimeUtils.toDateTimeFormat(transaction.dateTime)
      val query    =
        s"INSERT INTO $tableName ($DATE_TIME, $AMOUNT, $DATE, $HOUR, $CREATED_AT) VALUES('${dateTime}', ${transaction.amount}, '${transaction.date}', ${transaction.hour}, dateof(now()));"
      session.execute(query)
    }.toEither.left.map(ex => TransactionError(s"Unable to insert to DB: ${ex.getMessage}"))

  def getTransactionByHour(hour: Int): Seq[TransactionMetadata] = {
    val query = s"SELECT * from $tableName where $HOUR = $hour ALLOW FILTERING;"
    getAndBuildMetadata(query)
  }

  def getTransactionByDate(date: String): Seq[TransactionMetadata] = {
    val query = s"SELECT * from $tableName where $DATE = '$date' ALLOW FILTERING;"
    getAndBuildMetadata(query)
  }

  def getTransactionByPeriod(startDateTime: String, endDateTime: String): Seq[TransactionMetadata] = {
    val query = s"""
                   |SELECT * from $tableName
                   |where $DATE_TIME > '$startDateTime'
                   |and $DATE_TIME < '$endDateTime' ALLOW FILTERING;
                   |""".stripMargin
    getAndBuildMetadata(query)
  }

  private def getAndBuildMetadata(query: String): Seq[TransactionMetadata] = {
    val resultSet = session.execute(query)
    resultSet
      .iterator()
      .map(row => {
        TransactionMetadata(
          dateTime = DateTimeUtils.parseToJodaDateTime(row.getTimestamp(DATE_TIME)),
          amount = row.getDouble(AMOUNT),
          date = row.getDate(DATE).toString,
          hour = row.getInt(HOUR)
        )
      })
      .toSeq
  }

}
