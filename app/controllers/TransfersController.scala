package controllers

import javax.inject._

import data.AccountsStore
import dto._
import dto.Serialization._
import exceptions.TransferException
import model.ErrorCodes
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import services.TransferService

import scala.util.{Failure, Random, Success, Try}

/**
  * The controller for the transfer operations.
  *
  * @param cc The ControllerComponents.
  * @param store The accounts data store. This is a singleton object and it's shared by other controllers
  */
@Singleton
class TransfersController @Inject()(cc: ControllerComponents, store: AccountsStore) extends AbstractController(cc) {

  val service = new TransferService(store)

  /**
    * Route that top-ups an account with the amount provided in the body.
    * The body should be a [[TopUpDTO]] formatted as a json.
    * @return [[Ok]] if the top-up succeeded, or [[BadRequest]] with the errorCode otherwise.
    *        See [[ErrorCodes]] for more information about these error codes.
    */
  def topUp(): Action[JsValue] = Action(parse.json) { request =>
    withParsedRequest[TopUpDTO](request) { topUp =>
      service.topUp(topUp.account, topUp.amount).toResult
    }
  }

  /**
    * Route that transfers an amount provided in the body from an account 'FROM' to an account 'TO'.
    * The body should be a [[TransferDTO]] formatted as a json.
    * @return [[Ok]] if the transfer succeeded, or [[BadRequest]] with the errorCode otherwise.
    *        See [[ErrorCodes]] for more information about these error codes.
    */
  def transfer(): Action[JsValue] = Action(parse.json) { request =>
    withParsedRequest[TransferDTO](request) { transfer =>
      service.transfer(from = transfer.from, to = transfer.to, transfer.amount).toResult
    }
  }

  /**
    * Route that withdraws the amount provided in the body from an account.
    * The body should be a [[WithdrawDTO]] formatted as a json.
    * @return [[Ok]] if the withdraw succeeded, or [[BadRequest]] with the errorCode otherwise.
    *        See [[ErrorCodes]] for more information about these error codes.
    */
  def withdraw(): Action[JsValue] = Action(parse.json) { request =>
    withParsedRequest[WithdrawDTO](request) { withdraw =>
      service.withdraw(withdraw.account, withdraw.amount).toResult
    }
  }

  /**
    * Tries to deserialize the json body of the request provided and applies the operation if it succeeds.
    * This is a helper method to allow deserializing the request body and handle the errors when parsing it.
    * @param request The request that contains the body as json.
    * @param operation The operation to be applied if the object is deserialized.
    * @param reads The json Reads object for the required type.
    * @tparam T The type of the object to be deserialized.
    * @return The result of the operation if the deserialization succeeded or the parsing error otherwise.
    */
  private def withParsedRequest[T](request: Request[JsValue])(operation: (T) => Result)(implicit reads: Reads[T]): Result = {
    Json.fromJson[T](request.body) match {
      case JsSuccess(t, _) =>
        operation(t)
      case JsError(errors) =>
        Logger.error(s"Couldn't parse the json body for request '${request.path}'")
        errors.foreach{
          case (path, pathErrors) =>
            Logger.debug(s"\t$path")
            pathErrors.foreach(error => Logger.debug(s"\t\t${error.message}"))
        }
        BadRequest(Json.toJson(ErrorDTO("Couldn't parse the json body", ErrorCodes.ParsingError)))
    }
  }

  /**
    * Implicit class that allows converting [[Try]] results into play [[Result]].
    * @param triedResult The result of the operation.
    */
  private implicit class TryToResult(triedResult: Try[Unit]) {
    def toResult: Result = {
      triedResult match {
        case Success(_) =>
          Ok
        case Failure(TransferException(message, errorCode)) =>
          BadRequest(Json.toJson(ErrorDTO(message, errorCode)))
        case Failure(_) =>
          // An unknown error happened. In order to check in the logs we are going to provide a correlation id for this
          // request. This can be improved adding a correlation id that will be provided in the request (i.e. in the
          // headers) and then added to every log while processing it.
          val correlationId = Random.alphanumeric.take(20).mkString
          Logger.error(s"Unknown error: $correlationId")
          BadRequest(Json.toJson(ErrorDTO(s"Unknown error: $correlationId", ErrorCodes.Unknown)))
      }
    }
  }
}
