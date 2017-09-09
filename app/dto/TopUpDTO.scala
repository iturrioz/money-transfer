package dto

/**
  * The top-up request body.
  * @param account The account to be topped-up.
  * @param amount The amount defined as cents. See [[model.Account]] for more information.
  */
case class TopUpDTO(account: String, amount: Long)
