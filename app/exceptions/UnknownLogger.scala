package exceptions

import play.api.Logger

import scala.util.Random

object UnknownLogger {

  /**
    * Logs a new correlation id to allow tracing the error.
    * @param message The message to be logged with the id.
    * @param throwable The exception that occurred.
    * @return The generated correlation id.
    */
  def logError(message: String, throwable: Throwable): String = {
    // An unknown error happened. In order to check in the logs we are going to provide a correlation id for this
    // request. This can be improved adding a correlation id that will be provided in the request (i.e. in the
    // headers) and then added to every log while processing it.
    val correlationId = Random.alphanumeric.take(20).mkString
    Logger.error(s"$message: $correlationId")
    correlationId
  }
}
