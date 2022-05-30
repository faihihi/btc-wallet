package btc.db

import btc.DBSettings
import com.datastax.driver.core.{Cluster, Session}
import com.typesafe.scalalogging.LazyLogging
import btc.db.DBConstants._
import btc.model.TransactionError

import scala.util.Try

class DBConnector(dbSetting: DBSettings) extends LazyLogging {

  def connect(): Either[TransactionError, Session] = {
    Try {
      val cluster = Cluster
        .builder()
        .addContactPoint(dbSetting.host)
        .withPort(dbSetting.port)
        .build()

      val metadata = cluster.getMetadata
      logger.info(s"Connected to DB cluster: ${metadata.getClusterName}")

      val session = cluster.newSession()
      initializeDB(session)
    }.toEither.left.map(ex => TransactionError(ex.getMessage))
  }

  def close(session: Session): Unit = {
    val cluster = session.getCluster
    session.close()
    cluster.close()

    logger.info(
      s"Connection with ${session.getCluster.getClusterName} DB are closed"
    )
  }

  private def initializeDB(session: Session): Session = {
    val createKeyspaceQuery =
      s"""
        |CREATE KEYSPACE IF NOT EXISTS ${dbSetting.keyspace}
        |WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 3};
        |""".stripMargin

    val createTableQuery =
      s"""
         |CREATE TABLE IF NOT EXISTS ${dbSetting.tableName} (
         |    $DATE_TIME timestamp,
         |    $AMOUNT double,
         |    $CREATED_AT timestamp,
         |    PRIMARY KEY ($CREATED_AT)
         |);
         |""".stripMargin

    session.execute(createKeyspaceQuery)
    session.execute(s"USE ${dbSetting.keyspace}")
    session.execute(createTableQuery)
    logger.info(
      s"Using Keyspace: [${dbSetting.keyspace}] and Table: [${dbSetting.tableName}]"
    )
    session
  }

}
