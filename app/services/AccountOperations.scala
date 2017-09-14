package services

import javax.inject.Inject

import data.AccountsStore
import exceptions.TransferException
import model.{Account, ErrorCodes}

import scala.util.{Failure, Success, Try}

/**
  * The set of operations for the accounts available in the data store.
  * @param store The accounts data store.
  */
class AccountOperations @Inject() (store: AccountsStore) {

  /**
    * Top-ups an account if this exists in the data store.
    * @param id     The account identifier.
    * @param amount The amount in cents to be topped-up. See [[model.Account]] for more information.
    * @return A [[Success]] if the account was topped-up, or a [[Failure]] with the error code otherwise.
    *         See [[ErrorCodes]] for more information.
    */
  def topUp(id: String, amount: Long): Try[Unit] = {
    store.getAccount(id) match {
      case Some(account) =>
        Success(store.updateAccount(id, account.withTopUp(amount)))
      case None =>
        Failure(TransferException("Account doesn't exist", ErrorCodes.AccountNotAvailable))
    }
  }

  /**
    * Withdraws an account if this exists in the data store.
    * @param id     The account identifier.
    * @param amount The amount in cents to be removed. See [[model.Account]] for more information.
    * @return A [[Success]] if the withdraw succeeded, or a [[Failure]] with the error code otherwise.
    *         See [[ErrorCodes]] for more information.
    */
  def withdraw(id: String, amount: Long): Try[Unit] = {
    store.getAccount(id) match {
      case Some(account) =>
        account.withWithdrawal(amount).map(store.updateAccount(id, _))
      case None =>
        Failure(TransferException("Account doesn't exist", ErrorCodes.AccountNotAvailable))
    }
  }

  /**
    * Transfers an amount if the accounts exist in the data store.
    * @param from The account that the amount will be debited.
    * @param to The account that the amount will be credited.
    * @param amount The amount in cents to be transferred. See [[model.Account]] for more information.
    * @return A [[Success]] if the transfer succeeded, or a [[Failure]] with the error code otherwise.
    *         See [[ErrorCodes]] for more information.
    */
  def transfer(from: String, to: String, amount: Long): Try[Unit] = {
    (store.getAccount(from), store.getAccount(to)) match {
      case (Some(_), None) =>
        Failure(TransferException("To account doesn't exist", ErrorCodes.ToAccountNotAvailable))
      case (None, _) =>
        // If the FROM account is not available, we ignore if the TO exists, just for simplification.
        Failure(TransferException("From account doesn't exist", ErrorCodes.FromAccountNotAvailable))
      case (Some(fromAccount), Some(toAccount)) =>
        // We update the accounts only if the withdraw returned a Success. Because of the current simple implementation
        // we assume that the update and the top-up won't fail. If these change, this logic needs to be updated. It
        // would require compensation actions if any of the update fails.
        fromAccount.withWithdrawal(amount).map{ fromResult =>
          val toResult = toAccount.withTopUp(amount)
          store.updateAccount(from, fromResult)
          store.updateAccount(to, toResult)
        }
    }
  }

  /**
    * Creates an account in the store if it doesn't exist
    * @param id The identifier for the account to be created.
    * @return A [[Success]] if the account was created, or a [[Failure]] with the error code otherwise.
    *         See [[ErrorCodes]] for more information.
    */
  def create(id: String): Try[Unit] = {
    store.getAccount(id) match {
      case Some(_) =>
        Failure(TransferException("Account already exists", ErrorCodes.AccountAlreadyAvailable))
      case None =>
        store.updateAccount(id, Account(0))
        Success(())
    }
  }
}
