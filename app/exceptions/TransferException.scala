package exceptions

/**
  * The exception that is used when an error happened during any tranfer operation.
  * @param message The error message.
  * @param errorCode The error code. See [[model.ErrorCodes]] for more information.
  */
case class TransferException(message: String, errorCode: Int) extends Exception(message)
