package music.metadata.api.http.model

sealed trait AppError extends Throwable {
  val message: String
}
final case class DecodingError(message: String) extends AppError
final case class TransformingError(message: String) extends AppError

//at the moment we are using the domain errors as http error responses - this will change obviously with seperate of concern for error response body
sealed trait NonFatalError extends AppError
final case class DataNotFound(message: String) extends NonFatalError
final case class UnexpectedError(message: String) extends NonFatalError

