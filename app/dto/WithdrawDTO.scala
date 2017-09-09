package dto

/**
  * The withdraw request body.
  * @param account The account to withdraw.
  * @param amount The amount defined as cents. See [[model.Account]] for more information.
  */
case class WithdrawDTO(account: String, amount: Long)
