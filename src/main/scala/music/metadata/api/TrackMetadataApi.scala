package music.metadata.api

import cats.effect.Concurrent
import org.http4s.{HttpRoutes, Response}
import org.http4s.dsl.Http4sDsl
import music.metadata.api.http.model.{AppError, DataInvalid, DataNotFound, DecodingError, NewTrackRequest, NewTrackResponse, TracksResponse, TransformingError, UnexpectedError}
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.syntax.either._
import cats.syntax.applicativeError._
import io.circe.generic.auto._
import music.metadata.api.service.TrackService
import org.http4s.circe.CirceEntityCodec._

import java.util.UUID
import scala.util.{Failure, Success, Try}

object TrackMetadataApi {

  //TODO: log any errors -
  //ive not used the error messages from within UnexpectedError as i want client friendly messages returned back to the client and not internal error messages which i probably should log before going live
  def routes[F[_]: Concurrent](trackService: TrackService[F]): HttpRoutes[F] = {

    val dsl = new Http4sDsl[F]{}
    import dsl._

    def handleErrors(failure: AppError): F[Response[F]] = {
      failure match {
        case error: DecodingError =>
          BadRequest(error)
        case error: TransformingError =>
          BadRequest(error)
        //TODO: 2 tests are missing
        case error: DataInvalid =>
          BadRequest(error)
        case error: DataNotFound =>
          NotFound(error)
        case _: UnexpectedError =>
          InternalServerError(UnexpectedError("new track could not be saved"))
      }
    }

    HttpRoutes.of[F] {
      case req @ POST -> Root / "newtrack" =>
        for {
          requestBodyResult <- req.as[NewTrackRequest].attemptT.leftMap { failure => DecodingError(failure.getCause.getMessage)}.value
          maybeNewTrackValidated = requestBodyResult.flatMap(NewTrackRequest.toNewTrack(_).leftMap(TransformingError))
          resp <- maybeNewTrackValidated match {
                  case Right(newTrack) =>
                    trackService
                      .saveNewTrack(newTrack)
                      .flatMap(_ => Created(NewTrackResponse(newTrack.id)))
                      .handleErrorWith {
                        case error: DataInvalid =>
                          BadRequest(error)
                        case _ =>
                          InternalServerError(UnexpectedError("new track could not be saved"))
                      }
                  case Left(failure) =>
                    handleErrors(failure)
                }
        } yield resp
      case GET -> Root / "tracks" / artistIdPathParam => {
        Try(UUID.fromString(artistIdPathParam)) match {
          case Success(artistId) =>
            trackService
              .getTracks(artistId)
              .flatMap { tracks =>
                if (tracks.isEmpty) {
                  NotFound(TracksResponse.from(artistId, tracks))
                } else {
                  Ok(TracksResponse.from(artistId, tracks))
                }
              }
              .handleErrorWith { _ =>
                InternalServerError(UnexpectedError(s"something went wrong trying to retrieve tracks for artist [id=$artistId]"))
              }
          case Failure(error) =>
            BadRequest(DecodingError(error.getMessage))
        }
      }
    }
  }
}