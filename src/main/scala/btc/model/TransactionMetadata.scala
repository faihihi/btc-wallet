package btc.model

import org.joda.time.DateTime

final case class TransactionMetadata(dateTime: DateTime, amount: Double, date: String, hour: Int)
