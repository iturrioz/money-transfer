package services

import data.AccountsStore
import exceptions.TransferException
import model.Account
import org.scalatest.{MustMatchers, WordSpec}

import scala.util.Success

class TransferServiceSpec extends WordSpec with MustMatchers {

  "TransferService" when {
    "when calling topUp" should {
      "top-up an account" in new Fixture {
        store.updateAccount("test_account", Account(0))
        service.topUp("test_account", 100) mustBe Success(())

        store.getAccount("test_account") mustBe Some(Account(100))
      }
      "fail if the account doesn't exist" in new Fixture {
        private val result = service.topUp("test_account", 100)
        result.isFailure mustBe true
        result.failed.get mustBe a[TransferException]

        store.getAccount("test_account") mustBe None
      }
    }
    "when calling tranfer" should {
      "top-up an account" in new Fixture {
        store.updateAccount("from_test_account", Account(100))
        store.updateAccount("to_test_account", Account(5))
        service.transfer("from_test_account", "to_test_account", 40) mustBe Success(())

        store.getAccount("from_test_account") mustBe Some(Account(60))
        store.getAccount("to_test_account") mustBe Some(Account(45))
      }
      "fail if the FROM account doesn't exist" in new Fixture {
        store.updateAccount("to_test_account", Account(5))
        private val result = service.transfer("from_test_account", "to_test_account", 40)
        result.isFailure mustBe true
        result.failed.get mustBe a[TransferException]

        store.getAccount("from_test_account") mustBe None
        store.getAccount("to_test_account") mustBe Some(Account(5))
      }
      "fail if the TO account doesn't exist" in new Fixture {
        store.updateAccount("from_test_account", Account(100))
        private val result = service.transfer("from_test_account", "to_test_account", 40)
        result.isFailure mustBe true
        result.failed.get mustBe a[TransferException]

        store.getAccount("from_test_account") mustBe Some(Account(100))
        store.getAccount("to_test_account") mustBe None
      }
      "fail if none of the accounts exist" in new Fixture {
        private val result = service.transfer("from_test_account", "to_test_account", 40)
        result.isFailure mustBe true
        result.failed.get mustBe a[TransferException]

        store.getAccount("from_test_account") mustBe None
        store.getAccount("to_test_account") mustBe None
      }
    }
  }

  trait Fixture {
    val store = new AccountsStore
    val service = new TransferService(store)
  }
}
