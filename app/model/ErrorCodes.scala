package model

/**
  * The list of errors that allow the clients of the api recognize the possible issues on the calls.
  */
object ErrorCodes {
  val ParsingError = 1000

  val NotEnoughBalance = 2000
  val UnknownErrorOnOperation = 2001

  val AccountNotAvailable = 3000
  val ToAccountNotAvailable = 3001
  val FromAccountNotAvailable = 3002
  val AccountAlreadyAvailable = 3003

  val Unknown: Int = 9000
}
