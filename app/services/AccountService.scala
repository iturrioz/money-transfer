package services

import javax.inject._

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import services.AccountService._

import scala.concurrent.Future
import scala.concurrent.duration._

/**
  *  A service that allows making operations in the accounts available in the data store.
  * @param system The actor system.
  * @param accountsActor The actor that will handle the accounts state.
  */
@Singleton
class AccountService @Inject()(system: ActorSystem, @Named("accounts-actor") accountsActor: ActorRef) {

  implicit val timeout: Timeout = 5.seconds

  /**
    * Top-ups an account if this exists in the data store.
    *
    * @param id     The account identifier.
    * @param amount The amount in cents to be topped-up. See [[model.Account]] for more information.
    * @return A [[Future]] of a [[OperationResponse]] with the result.
    */
  def topUp(id: String, amount: Long): Future[OperationResponse] = {
    (accountsActor ? TopUp(id, amount)).mapTo[OperationResponse]
  }

  /**
    * Withdraws an account if this exists in the data store.
    *
    * @param id     The account identifier.
    * @param amount The amount in cents to be removed. See [[model.Account]] for more information.
    * @return A [[Future]] of a [[OperationResponse]] with the result.
    */
  def withdraw(id: String, amount: Long): Future[OperationResponse] = {
    (accountsActor ? Withdraw(id, amount)).mapTo[OperationResponse]
  }

  /**
    * Transfers an amount if the accounts exist in the data store.
    *
    * @param from The account that the amount will be debited.
    * @param to The account that the amount will be credited.
    * @param amount The amount in cents to be transferred. See [[model.Account]] for more information.
    * @return A [[Future]] of a [[OperationResponse]] with the result.
    */
  def transfer(from: String, to: String, amount: Long): Future[OperationResponse] = {
    (accountsActor ? Transfer(from, to, amount)).mapTo[OperationResponse]

  }

  /**
    * Creates an account in the store.
    *
    * @param id The id of the account to be created.
    * @return A [[Future]] of a [[OperationResponse]] with the result.
    */
  def create(id: String): Future[OperationResponse] = {
    (accountsActor ? Create(id)).mapTo[OperationResponse]

  }
}

object AccountService {

  case class Create(id: String)
  case class TopUp(id: String, amount: Long)
  case class Withdraw(id: String, amount: Long)
  case class Transfer(from: String, to: String, amount: Long)

  trait OperationResponse
  case object OperationSucceeded extends OperationResponse
  case class OperationFailed(message: String, errorCode: Int) extends OperationResponse
}
