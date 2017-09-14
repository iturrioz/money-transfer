package services

import javax.inject._

import akka.actor.Actor
import exceptions.{TransferException, UnknownLogger}
import model.ErrorCodes
import services.AccountService._

import scala.util.{Failure, Success, Try}

/**
  * The actor that handles the state of the accounts.
  * Since this actor is going to be a singleton, he will the only one allowed to make changes in the accounts. All the
  * operations are executed synchronously.
  * @param operations The operations that are allowed in the accounts.
  */
@Singleton
class AccountsActor @Inject()(operations: AccountOperations) extends Actor {
  override def receive: Receive = {
    case TopUp(id, amount) =>
      operations.topUp(id, amount).forwardToSender()
    case Withdraw(id, amount) =>
      operations.withdraw(id, amount).forwardToSender()
    case Transfer(from, to, amount) =>
      operations.transfer(from = from, to = to, amount).forwardToSender()
    case Create(id) =>
      operations.create(id).forwardToSender()
  }

  /**
    * This is a helper class to forward [[scala.concurrent.Future]] of [[Any]] result as a [[OperationResponse]].
    * @param result The result of the operation
    */
  implicit class ResultToSender(result: Try[Unit]) {
    def forwardToSender(): Unit = {
      result match {
        case Success(_) =>
          sender ! OperationSucceeded
        case Failure(TransferException(message, errorCode)) =>
          sender ! OperationFailed(message, errorCode)
        case Failure(t) =>
          val correlationId = UnknownLogger.logError(s"Unknown exception on operation", t)
          sender ! OperationFailed(s"Unknown exception on operation: $correlationId", ErrorCodes.UnknownErrorOnOperation)
      }
    }
  }
}