package music.metadata.api.http.model

sealed trait AppError
final case class AppErrorResponse(message: String) extends AppError

