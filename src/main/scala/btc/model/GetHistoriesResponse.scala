package btc.model

final case class GetHistoriesResponse(
    transactions: Seq[BTCTransaction],
    error: Option[String]
)
