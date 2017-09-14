package controllers

import javax.inject._

import dto._
import dto.Serialization._
import exceptions.UnknownLogger
import model.ErrorCodes
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import services.AccountService
import services.AccountService.{OperationFailed, OperationResponse, OperationSucceeded}

import scala.concurrent.{ExecutionContext, Future}

/**
  * The controller for the transfer operations.
  *
  * @param cc The ControllerComponents.
  */
@Singleton
class TransfersController @Inject()(cc: ControllerComponents, service: AccountService)(implicit executionContext: ExecutionContext) extends AbstractController(cc) {

  /**
    * Route that top-ups an account with the amount provided in the body.
    * The body should be a [[TopUpDTO]] formatted as a json.
    * @return [[Ok]] if the top-up succeeded, or [[BadRequest]] with the errorCode otherwise.
    *        See [[ErrorCodes]] for more information about these error codes.
    */
  def topUp(): Action[JsValue] = Action.async(parse.json) { request =>
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
  def transfer(): Action[JsValue] = Action.async(parse.json) { request =>
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
  def withdraw(): Action[JsValue] = Action.async(parse.json) { request =>
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
  private def withParsedRequest[T](request: Request[JsValue])(operation: (T) => Future[Result])(implicit reads: Reads[T]): Future[Result] = {
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
        Future.successful(BadRequest(Json.toJson(ErrorDTO("Couldn't parse the json body", ErrorCodes.ParsingError))))
    }
  }

  /**
    * Implicit class that allows converting [[Future]]s of [[OperationResponse]] into play [[Result]] [[Future]].
    * @param eventualResult The result of the operation.
    */
  private implicit class TryToResult(eventualResult: Future[OperationResponse]) {
    def toResult: Future[Result] = {
      eventualResult.map{
        case OperationSucceeded =>
          Ok
        case OperationFailed(message, errorCode) =>
          BadRequest(Json.toJson(ErrorDTO(message, errorCode)))
      }.recover{
        case t =>
          val correlationId = UnknownLogger.logError(s"Unknown error", t)
          BadRequest(Json.toJson(ErrorDTO(s"Unknown error: $correlationId", ErrorCodes.Unknown)))
      }
    }
  }
}
