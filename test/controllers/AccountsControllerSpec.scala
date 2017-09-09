package controllers

import data.AccountsStore
import dto.{BalanceDTO, ErrorDTO}
import model.{Account, ErrorCodes}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.{Json, Reads}
import play.api.test.{FakeRequest, Injecting}
import play.api.test.Helpers._

class AccountsControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {
  implicit val errorReads: Reads[ErrorDTO] = Json.reads[ErrorDTO]

  "AccountsController" when {
    "receiving a create account request" should {
      "return Created when the account is created" in new Fixture {
        private val result = route(app, FakeRequest(POST, controllers.routes.AccountsController.create("test_account").url)).get

        status(result) mustBe CREATED
        headers(result).get("Location") mustBe Some("accounts/test_account")
        store.getAccount("test_account") mustBe Some(Account(0))
      }

      "return BadRequest with the AccountAlreadyAvailable error code if the account already exists" in new Fixture {
        store.updateAccount("test_account", Account(100))

        private val result = route(app, FakeRequest(POST, controllers.routes.AccountsController.create("test_account").url)).get

        status(result) mustBe BAD_REQUEST
        Json.fromJson[ErrorDTO](contentAsJson(result)).get.errorCode mustBe ErrorCodes.AccountAlreadyAvailable
        store.getAccount("test_account") mustBe Some(Account(100))
      }
      "receiving a get balance request" should {
        implicit val balanceReads: Reads[BalanceDTO] = Json.reads[BalanceDTO]
        "return Ok when the account exists" in new Fixture {
          store.updateAccount("test_account", Account(100))

          private val result = route(app, FakeRequest(GET, controllers.routes.AccountsController.balance("test_account").url)).get

          status(result) mustBe OK
          Json.fromJson[BalanceDTO](contentAsJson(result)).get.balance mustBe 100
        }

        "return NotFound with the AccountNotAvailable error code if the account doesn't exist" in new Fixture {
          private val result = route(app, FakeRequest(GET, controllers.routes.AccountsController.balance("test_account").url)).get

          status(result) mustBe BAD_REQUEST
          Json.fromJson[ErrorDTO](contentAsJson(result)).get.errorCode mustBe ErrorCodes.AccountNotAvailable
          store.getAccount("test_account") mustBe None
        }
      }
    }
  }


  trait Fixture {
    val store: AccountsStore = inject[AccountsStore]
  }
}
