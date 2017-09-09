package dto

/**
  * The transfer request body.
  * @param from The account to be debited.
  * @param to The account to be credited.
  * @param amount The amount defined as cents. See [[model.Account]] for more information.
  */
case class TransferDTO(from: String, to: String, amount: Long)
