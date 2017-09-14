package controllers

import javax.inject._

import data.AccountsStore
import dto.Serialization._
import dto._
import exceptions.UnknownLogger
import model.ErrorCodes
import play.api.libs.json._
import play.api.mvc._
import services.AccountService
import services.AccountService.{OperationFailed, OperationSucceeded}

import scala.concurrent.ExecutionContext

/**
  * The controller for the account operations.
  *
  * @param cc The ControllerComponents.
  * @param store The accounts data store. This is a singleton object and it's shared by other controllers.
  * @param service The account service that allows applying operations in a concurrent way.
  * @param executionContext The execution context for the futures.
  */
@Singleton
class AccountsController @Inject()(cc: ControllerComponents, store: AccountsStore, service: AccountService)(
  implicit executionContext: ExecutionContext) extends AbstractController(cc) {

  /**
    * Route that creates a new account for the provided id.
    * @param id The identifier of the account.
    * @return [[Created]] with the Location header if the account has been created, or [[BadRequest]] otherwise.
    */
  def create(id: String): Action[AnyContent] = Action.async { request =>
    service.create(id).map{
      case OperationSucceeded =>
        Created.withHeaders(("Location", s"accounts/$id"))
      case OperationFailed(message, errorCode) =>
        BadRequest(Json.toJson(ErrorDTO(message, errorCode)))
    }.recover{
      case t =>
        val correlationId = UnknownLogger.logError("Future failed when creating account", t)
        BadRequest(Json.toJson(ErrorDTO(s"Future failed when creating account: $correlationId", ErrorCodes.Unknown)))
    }
  }

  /**
    * Route that returns the balance of the account for the provided id.
    * @param id The identifier of the account.
    * @return [[Ok]] with the balance of the account in the response body, or [[BadRequest]] otherwise.
    */
  def balance(id: String): Action[AnyContent] = Action {
    store.getAccount(id) match {
      case Some(account) =>
        Ok(Json.toJson(BalanceDTO(account.balance)))
      case None =>
        BadRequest(Json.toJson(ErrorDTO("Account doesn't exist", ErrorCodes.AccountNotAvailable)))
    }
  }
}
