package controllers

import javax.inject._

import data.AccountsStore
import dto.Serialization._
import dto._
import model.{Account, ErrorCodes}
import play.api.libs.json._
import play.api.mvc._

/**
  * The controller for the account operations.
  *
  * @param cc The ControllerComponents.
  * @param store The accounts data store. This is a singleton object and it's shared by other controllers
  */
@Singleton
class AccountsController @Inject()(cc: ControllerComponents, store: AccountsStore) extends AbstractController(cc) {

  /**
    * Route that creates a new account for the provided id.
    * @param id The identifier of the account.
    * @return [[Created]] with the Location header if the account has been created, or [[BadRequest]] otherwise.
    */
  def create(id: String): Action[AnyContent] = Action {
    store.getAccount(id) match {
      case Some(_) =>
        BadRequest(Json.toJson(ErrorDTO("Account already exists", ErrorCodes.AccountAlreadyAvailable)))
      case None =>
        store.updateAccount(id, Account(0))
        Created.withHeaders(("Location", s"accounts/$id"))
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
