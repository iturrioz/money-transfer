package controllers

import akka.stream.Materializer
import data.AccountsStore
import dto.{ErrorDTO, TopUpDTO, TransferDTO, WithdrawDTO}
import model.{Account, ErrorCodes}
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json.{Json, Reads}
import play.api.test._
import play.api.test.Helpers._

class TransfersControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {
  implicit val errorReads: Reads[ErrorDTO] = Json.reads[ErrorDTO]

  "TransfersController" when {
    "receiving a TopUp request" should {
      implicit val writer = Json.writes[TopUpDTO]

      "return Ok when the top up succeeds" in new Fixture {
        store.updateAccount("test_account", Account(100))

        private val result = route(app, FakeRequest(POST, controllers.routes.TransfersController.topUp().url)
          .withJsonBody(Json.toJson(TopUpDTO("test_account", 1000)))).get

        status(result) mustBe OK
        store.getAccount("test_account") mustBe Some(Account(1100))
      }

      "return BadRequest with the AccountNotAvailable error code if the account doesn't exist" in new Fixture {
        private val result = route(app, FakeRequest(POST, controllers.routes.TransfersController.topUp().url)
          .withJsonBody(Json.toJson(TopUpDTO("test_account", 1000)))).get

        status(result) mustBe BAD_REQUEST
        Json.fromJson[ErrorDTO](contentAsJson(result)).get.errorCode mustBe ErrorCodes.AccountNotAvailable
        store.getAccount("test_account") mustBe None
      }
    }
    "receiving a Withdraw request" should {
      implicit val writer = Json.writes[WithdrawDTO]

      "return Ok when the withdraw succeeds" in new Fixture {
        store.updateAccount("test_account", Account(100))

        private val result = route(app, FakeRequest(POST, controllers.routes.TransfersController.withdraw().url)
          .withJsonBody(Json.toJson(WithdrawDTO("test_account", 55)))).get

        status(result) mustBe OK
        store.getAccount("test_account") mustBe Some(Account(45))
      }

      "return BadRequest with the NotEnoughBalance error code if the account doesn't have enough balance" in new Fixture {
        store.updateAccount("test_account", Account(100))

        private val result = route(app, FakeRequest(POST, controllers.routes.TransfersController.withdraw().url)
          .withJsonBody(Json.toJson(WithdrawDTO("test_account", 200)))).get

        status(result) mustBe BAD_REQUEST
        Json.fromJson[ErrorDTO](contentAsJson(result)).get.errorCode mustBe ErrorCodes.NotEnoughBalance
        store.getAccount("test_account") mustBe Some(Account(100))
      }

      "return BadRequest with the AccountNotAvailable error code if the account doesn't exist" in new Fixture {
        private val result = route(app, FakeRequest(POST, controllers.routes.TransfersController.withdraw().url)
          .withJsonBody(Json.toJson(WithdrawDTO("test_account", 1000)))).get

        status(result) mustBe BAD_REQUEST
        Json.fromJson[ErrorDTO](contentAsJson(result)).get.errorCode mustBe ErrorCodes.AccountNotAvailable
        store.getAccount("test_account") mustBe None
      }
    }
    "receiving a Transfer request" should {
      implicit val writer = Json.writes[TransferDTO]

      "return Ok when the transfer succeeds" in new Fixture {
        store.updateAccount("from_test_account", Account(100))
        store.updateAccount("to_test_account", Account(20))

        private val result = route(app, FakeRequest(POST, controllers.routes.TransfersController.transfer().url)
          .withJsonBody(Json.toJson(TransferDTO("from_test_account", "to_test_account", 55)))).get

        status(result) mustBe OK
        store.getAccount("from_test_account") mustBe Some(Account(45))
        store.getAccount("to_test_account") mustBe Some(Account(75))
      }

      "return BadRequest with the NotEnoughBalance error code if the FROM account doesn't have enough balance" in new Fixture {
        store.updateAccount("from_test_account", Account(100))
        store.updateAccount("to_test_account", Account(20))

        private val result = route(app, FakeRequest(POST, controllers.routes.TransfersController.transfer().url)
          .withJsonBody(Json.toJson(TransferDTO("from_test_account", "to_test_account", 200)))).get

        status(result) mustBe BAD_REQUEST
        Json.fromJson[ErrorDTO](contentAsJson(result)).get.errorCode mustBe ErrorCodes.NotEnoughBalance
        store.getAccount("from_test_account") mustBe Some(Account(100))
        store.getAccount("to_test_account") mustBe Some(Account(20))
      }

      "return BadRequest with the FromAccountNotAvailable error code if the FROM account doesn't exist" in new Fixture {
        store.updateAccount("to_test_account", Account(20))

        private val result = route(app, FakeRequest(POST, controllers.routes.TransfersController.transfer().url)
          .withJsonBody(Json.toJson(TransferDTO("from_test_account", "to_test_account", 200)))).get

        status(result) mustBe BAD_REQUEST
        Json.fromJson[ErrorDTO](contentAsJson(result)).get.errorCode mustBe ErrorCodes.FromAccountNotAvailable
        store.getAccount("from_test_account") mustBe None
        store.getAccount("to_test_account") mustBe Some(Account(20))
      }

      "return BadRequest with the FromAccountNotAvailable error code if none of the accounts exist" in new Fixture {
        private val result = route(app, FakeRequest(POST, controllers.routes.TransfersController.transfer().url)
          .withJsonBody(Json.toJson(TransferDTO("from_test_account", "to_test_account", 200)))).get

        status(result) mustBe BAD_REQUEST
        Json.fromJson[ErrorDTO](contentAsJson(result)).get.errorCode mustBe ErrorCodes.FromAccountNotAvailable
        store.getAccount("from_test_account") mustBe None
        store.getAccount("to_test_account") mustBe None
      }

      "return BadRequest with the ToAccountNotAvailable error code if the TO account doesn't exist" in new Fixture {
        store.updateAccount("from_test_account", Account(100))

        private val result = route(app, FakeRequest(POST, controllers.routes.TransfersController.transfer().url)
          .withJsonBody(Json.toJson(TransferDTO("from_test_account", "to_test_account", 200)))).get

        status(result) mustBe BAD_REQUEST
        Json.fromJson[ErrorDTO](contentAsJson(result)).get.errorCode mustBe ErrorCodes.ToAccountNotAvailable
        store.getAccount("from_test_account") mustBe Some(Account(100))
        store.getAccount("to_test_account") mustBe None
      }
    }
    "receiving a parsing" should {
      "return BadRequest with the ParsingError error code if the json is not a TopUpDTO" in new Fixture {
        private val result = route(app, FakeRequest(POST, controllers.routes.TransfersController.topUp().url)
          .withJsonBody(Json.parse("""{"valid": "json", "invalid": "TopUpDTO"}"""))).get

        status(result) mustBe BAD_REQUEST
        Json.fromJson[ErrorDTO](contentAsJson(result)).get.errorCode mustBe ErrorCodes.ParsingError
      }
      "return BadRequest with the ParsingError error code if the json is not a WithdrawDTO" in new Fixture {
        private val result = route(app, FakeRequest(POST, controllers.routes.TransfersController.withdraw().url)
          .withJsonBody(Json.parse("""{"valid": "json", "invalid": "WithdrawDTO"}"""))).get

        status(result) mustBe BAD_REQUEST
        Json.fromJson[ErrorDTO](contentAsJson(result)).get.errorCode mustBe ErrorCodes.ParsingError
      }
      "return BadRequest with the ParsingError error code if the json is not a TransferDTO" in new Fixture {
        private val result = route(app, FakeRequest(POST, controllers.routes.TransfersController.transfer().url)
          .withJsonBody(Json.parse("""{"valid": "json", "invalid": "TransferDTO"}"""))).get

        status(result) mustBe BAD_REQUEST
        Json.fromJson[ErrorDTO](contentAsJson(result)).get.errorCode mustBe ErrorCodes.ParsingError
      }
    }
  }

  trait Fixture {
    implicit val materializer: Materializer = app.materializer

    val store: AccountsStore = inject[AccountsStore]
  }
}
