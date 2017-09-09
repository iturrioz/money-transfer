package model

import exceptions.TransferException

import scala.util.{Failure, Try}

/**
  * An account of a user. This will track the balance available in it.
  * The balance will be defined by the amount of cents to avoid precision loss with floating types.
  * i.e. 5.65$ will be defined as 565.
  * Warning: Some currencies have 3 decimal places.
  *
  * @param balance The available balance in the account.
  */
case class Account(balance: Long) {

  /**
    * Adds the provided amount to the existing balance.
    * @param amount The amount to be added.
    * @return A new instance of [[Account]] with the updated balance.
    */
  def withTopUp(amount: Long): Account = {
    Account(balance + amount)
  }

  /**
    * Withdraws the provided amount from the existing balance if this is enough.
    * The current implementation doesn't allow negative balance. This can be changed depending on the requirements.
    * @param amount The amount to be removed.
    * @return A new instance of [[Account]] with the updated balance.
    */
  def withWithdrawal(amount: Long): Try[Account] = {
    if (amount > balance) Failure(TransferException("Not enough balance", ErrorCodes.NotEnoughBalance))
    else Try(Account(balance - amount))
  }
}
