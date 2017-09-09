package dto

/**
  * The error as a result of a request.
  * @param message The error message.
  * @param errorCode The error code. See [[model.ErrorCodes]] for more information.
  */
case class ErrorDTO(message: String, errorCode: Int)
