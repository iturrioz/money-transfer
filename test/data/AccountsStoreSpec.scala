package data

import model.Account
import org.scalatest.{MustMatchers, WordSpec}

class AccountsStoreSpec extends WordSpec with MustMatchers {

  "AccountsStore" should {
    "return None if the account doesn't exist" in new Fixture {
      store.getAccount("test_account") mustBe None
    }
    "store new accounts and get them" in new Fixture {
      store.updateAccount("test_account", Account(0))

      store.getAccount("test_account") mustBe Some(Account(0))
    }
    "update stored accounts" in new Fixture {
      store.updateAccount("test_account", Account(0))
      store.updateAccount("test_account", Account(10))

      store.getAccount("test_account") mustBe Some(Account(10))
    }
  }

  trait Fixture {
    val store = new AccountsStore
  }
}
