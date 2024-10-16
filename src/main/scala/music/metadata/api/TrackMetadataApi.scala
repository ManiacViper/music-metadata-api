package music.metadata.api

import cats.effect.Concurrent
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import music.metadata.api.http.model.{NewTrackErrorResponse, NewTrackRequest, NewTrackResponse}
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.syntax.either._
import cats.syntax.applicativeError._
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec._
import NewTrackRequest.decodeUUID

object TrackMetadataApi {

  def routes[F[_]: Concurrent](trackService: TrackRepository[F]): HttpRoutes[F] = {

    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case req @ POST -> Root / "newtrack" =>
        for {
          maybeNewTrackRequest <- req.as[NewTrackRequest].attemptT.leftMap { failure => NewTrackErrorResponse(failure.getCause.getMessage)}.value
          maybeTrackCreated = maybeNewTrackRequest.flatMap(NewTrackRequest.toNewTrack(_).leftMap(NewTrackErrorResponse))
          resp <- maybeTrackCreated match {
                  case Right(newTrack) =>
                    trackService
                      .create(newTrack)
                      .flatMap(_ => Created(NewTrackResponse(newTrack.id)))
                  case Left(error) =>
                    BadRequest(error)
                }
        } yield resp
    }
  }
}