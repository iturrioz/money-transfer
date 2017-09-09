package data

import javax.inject.Singleton

import model.Account

/**
  * Simple in-memory accounts data store.
  */
@Singleton
class AccountsStore {
  private val accounts = collection.mutable.Map[String, Account]()

  /**
    * Gets the account details for the provided id.
    * @param id The account identifier.
    * @return The account if it exists, or None otherwise.
    */
  def getAccount(id: String): Option[Account] = {
    accounts.get(id)
  }

  /**
    * Sets the provided account in the data store.
    * @param id The account identifier.
    * @param account The updated/new account to be set.
    */
  def updateAccount(id: String, account: Account): Unit = {
    accounts.put(id, account)
  }
}
