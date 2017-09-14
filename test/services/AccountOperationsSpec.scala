package services

import data.AccountsStore
import exceptions.TransferException
import model.Account
import org.scalatest.{MustMatchers, WordSpec}

import scala.util.Success

class AccountOperationsSpec extends WordSpec with MustMatchers {

  "AccountOperations" when {
    "calling topUp" should {
      "top-up an account" in new Fixture {
        store.updateAccount("test_account", Account(0))
        operations.topUp("test_account", 100) mustBe Success(())

        store.getAccount("test_account") mustBe Some(Account(100))
      }
      "fail if the account doesn't exist" in new Fixture {
        private val result = operations.topUp("test_account", 100)
        result.isFailure mustBe true
        result.failed.get mustBe a[TransferException]

        store.getAccount("test_account") mustBe None
      }
    }
    "calling withdraw" should {
      "withdraw the amount" in new Fixture {
        store.updateAccount("test_account", Account(50))
        operations.withdraw("test_account", 20) mustBe Success(())

        store.getAccount("test_account") mustBe Some(Account(30))
      }
      "fail if the account doesn't exist" in new Fixture {
        private val result = operations.withdraw("test_account", 100)
        result.isFailure mustBe true
        result.failed.get mustBe a[TransferException]

        store.getAccount("test_account") mustBe None
      }
      "fail if the balance is not enough" in new Fixture {
        store.updateAccount("test_account", Account(50))
        private val result = operations.withdraw("test_account", 100)
        result.isFailure mustBe true
        result.failed.get mustBe a[TransferException]

        store.getAccount("test_account") mustBe Some(Account(50))
      }
    }
    "calling tranfer" should {
      "transfer the amount" in new Fixture {
        store.updateAccount("from_test_account", Account(100))
        store.updateAccount("to_test_account", Account(5))
        operations.transfer("from_test_account", "to_test_account", 40) mustBe Success(())

        store.getAccount("from_test_account") mustBe Some(Account(60))
        store.getAccount("to_test_account") mustBe Some(Account(45))
      }
      "fail if the FROM account doesn't exist" in new Fixture {
        store.updateAccount("to_test_account", Account(5))
        private val result = operations.transfer("from_test_account", "to_test_account", 40)
        result.isFailure mustBe true
        result.failed.get mustBe a[TransferException]

        store.getAccount("from_test_account") mustBe None
        store.getAccount("to_test_account") mustBe Some(Account(5))
      }
      "fail if the TO account doesn't exist" in new Fixture {
        store.updateAccount("from_test_account", Account(100))
        private val result = operations.transfer("from_test_account", "to_test_account", 40)
        result.isFailure mustBe true
        result.failed.get mustBe a[TransferException]

        store.getAccount("from_test_account") mustBe Some(Account(100))
        store.getAccount("to_test_account") mustBe None
      }
      "fail if none if the accounts exist" in new Fixture {
        private val result = operations.transfer("from_test_account", "to_test_account", 40)
        result.isFailure mustBe true
        result.failed.get mustBe a[TransferException]

        store.getAccount("from_test_account") mustBe None
        store.getAccount("to_test_account") mustBe None
      }
    }
    "calling create" should {
      "create an account" in new Fixture {
        operations.create("test_account") mustBe Success(())
      }
      "fail if the account already exists" in new Fixture {
        store.updateAccount("test_account", Account(5))
        private val result = operations.create("test_account")
        result.isFailure mustBe true
        result.failed.get mustBe a[TransferException]

        store.getAccount("test_account") mustBe Some(Account(5))
      }
    }
  }

  trait Fixture {
    val store = new AccountsStore
    val operations = new AccountOperations(store)
  }
}
