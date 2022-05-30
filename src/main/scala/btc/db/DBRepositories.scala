package btc.db

import btc.DBSettings
import btc.db.DBConstants._
import btc.model.{BTCTransaction, TransactionError}
import com.datastax.driver.core.{ResultSet, Session}
import com.typesafe.scalalogging.LazyLogging

import scala.collection.convert.ImplicitConversions.`iterator asScala`
import scala.util.Try

class DBRepositories(session: Session, dbSetting: DBSettings)
    extends LazyLogging {

  val tableName = dbSetting.tableName

  def insertTransaction(
      transaction: BTCTransaction
  ): Either[TransactionError, ResultSet] =
    Try {
      session.execute(
        s"INSERT INTO $tableName ($DATE_TIME, $AMOUNT, $CREATED_AT) VALUES('${transaction.datetime}', ${transaction.amount}, dateof(now()));"
      )
    }.toEither.left.map(ex =>
      TransactionError(s"Unable to insert to DB: ${ex.getMessage}")
    )

  def getTransactionHistories(
      startDateTime: String,
      endDateTime: String
  ): Either[TransactionError, Seq[BTCTransaction]] =
    Try {
      val query =
        s"""
           |SELECT * from $tableName
           |where $DATE_TIME > '$startDateTime'
           |and $DATE_TIME < '$endDateTime' ALLOW FILTERING;
           |""".stripMargin
      val resultSet = session.execute(query)
      resultSet
        .iterator()
        .map(row => {
          BTCTransaction(
            datetime = row.getTimestamp(DATE_TIME).toString,
            amount = row.getDouble(AMOUNT)
          )
        })
        .toSeq
    }.toEither.left.map(ex =>
      TransactionError(s"Unable to fetch from DB: ${ex.getMessage}")
    )

}
