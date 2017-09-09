package controllers

import akka.stream.Materializer
import dto._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.{Json, Reads}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Injecting}

class MultiControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "TransfersController and AccountsController" should {
    "make the whole flow" in {
      implicit val materializer: Materializer = app.materializer
      implicit val balanceReads: Reads[BalanceDTO] = Json.reads[BalanceDTO]
      implicit val topUpWriter = Json.writes[TopUpDTO]
      implicit val transferWriter = Json.writes[TransferDTO]
      implicit val withdrawWriter = Json.writes[WithdrawDTO]

      val createdFrom = route(app, FakeRequest(POST, controllers.routes.AccountsController.create("from_test_account").url)).get
      status(createdFrom) mustBe CREATED

      val createdTo = route(app, FakeRequest(POST, controllers.routes.AccountsController.create("to_test_account").url)).get
      status(createdTo) mustBe CREATED

      val topUp = route(app, FakeRequest(POST, controllers.routes.TransfersController.topUp().url)
        .withJsonBody(Json.toJson(TopUpDTO("from_test_account", 1000)))).get
      status(topUp) mustBe OK

      val transfer = route(app, FakeRequest(POST, controllers.routes.TransfersController.transfer().url)
        .withJsonBody(Json.toJson(TransferDTO("from_test_account", "to_test_account", 600)))).get
      status(transfer) mustBe OK

      val withdraw = route(app, FakeRequest(POST, controllers.routes.TransfersController.withdraw().url)
        .withJsonBody(Json.toJson(WithdrawDTO("to_test_account", 300)))).get
      status(withdraw) mustBe OK

      val fromBalance = route(app, FakeRequest(GET, controllers.routes.AccountsController.balance("from_test_account").url)).get
      Json.fromJson[BalanceDTO](contentAsJson(fromBalance)).get.balance mustBe 400

      val toBalance = route(app, FakeRequest(GET, controllers.routes.AccountsController.balance("to_test_account").url)).get
      Json.fromJson[BalanceDTO](contentAsJson(toBalance)).get.balance mustBe 300
    }
  }
}
