package btc.model

final case class SaveTransactionResponse(
    success: Boolean,
    message: String,
    error: Option[String]
)
