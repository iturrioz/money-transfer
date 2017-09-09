package dto

import play.api.libs.json.{Json, Reads, Writes}

/**
  * Serializers and deserializers for DTO to Json and Json to DTO.
  */
object Serialization {
  implicit val topUpReads: Reads[TopUpDTO] = Json.reads[TopUpDTO]
  implicit val transferReads: Reads[TransferDTO] = Json.reads[TransferDTO]
  implicit val withdrawReads: Reads[WithdrawDTO] = Json.reads[WithdrawDTO]

  implicit val errorWrites: Writes[ErrorDTO] = Json.writes[ErrorDTO]
  implicit val balanceWrites: Writes[BalanceDTO] = Json.writes[BalanceDTO]
}
