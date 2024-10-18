package music.metadata.api

import cats.effect.Concurrent
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import music.metadata.api.http.model.{AppError, BodyDecodingError, DataNotFound, NewTrackRequest, NewTrackResponse, TrackResponse, TracksResponse, TransformingError, UnexpectedError}
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.syntax.either._
import cats.syntax.applicativeError._
import io.circe.generic.auto._
import music.metadata.api.domain.{Disco, Hiphop}
import music.metadata.api.repository.TrackRepository
import org.http4s.circe.CirceEntityCodec._

import java.util.UUID

object TrackMetadataApi {

  //TODO: ArtistAndTrackService - to check if artist id exists as we are assuming the artist id passed exists in the system atm
  //TODO: log any errors
  def routes[F[_]: Concurrent](trackRepository: TrackRepository[F]): HttpRoutes[F] = {

    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case req @ POST -> Root / "newtrack" =>
        for {
          requestBodyResult <- req.as[NewTrackRequest].attemptT.leftMap { failure => BodyDecodingError(failure.getCause.getMessage)}.value
          maybeTrackCreated = requestBodyResult.flatMap(NewTrackRequest.toNewTrack(_).leftMap(TransformingError))
          resp <- maybeTrackCreated match {
                  case Right(newTrack) =>
                    trackRepository
                      .create(newTrack)
                      .flatMap(_ => Created(NewTrackResponse(newTrack.id)))
                      .handleErrorWith { _ =>
                        InternalServerError(UnexpectedError("new track could not be saved"))
                      }
                  case Left(failure: AppError) =>
                    failure match {
                      case error: BodyDecodingError =>
                        BadRequest(error)
                      case error: TransformingError =>
                        BadRequest(error)
                      //TODO: 2 tests are missing
                      case error: DataNotFound =>
                        NotFound(error)
                      case _: UnexpectedError =>
                        InternalServerError(UnexpectedError("new track could not be saved"))
                    }
                }
        } yield resp
      case GET -> Root / "tracks" / UUIDVar(artistId) =>
        Ok(
          TracksResponse(
            artistId,
            List(
              TrackResponse(UUID.randomUUID(),"some-title-1", Hiphop, 200),
              TrackResponse(UUID.randomUUID(),"some-title-2", Disco, 100)
            )
          )
        )
    }
  }
}