package music.metadata.api

import cats.data.EitherT
import cats.effect.Concurrent
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import music.metadata.api.http.model.{NewTrackErrorResponse, NewTrackRequest, NewTrackResponse}
import cats.syntax.functor._
import cats.syntax.flatMap._
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec._

object TrackMetadataApi {

  def routes[F[_]: Concurrent](trackService: TrackService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case req @ POST -> Root / "newtrack" =>
        for {
          newTrackRequest <- req.as[NewTrackRequest]
          maybeTrackCreated <- EitherT.fromEither(NewTrackRequest.toNewTrack(newTrackRequest)).value
          resp <- maybeTrackCreated match {
                  case Right(newTrack) =>
                    trackService
                      .create(newTrack)
                      .flatMap(_ => Created(NewTrackResponse(newTrack.id)))
                  case Left(error) =>
                    BadRequest(NewTrackErrorResponse(error))
                }
        } yield resp
    }
  }
}