package music.metadata.api

import cats.effect.Concurrent
import cats.syntax.applicativeError._
import org.http4s.Response
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.circe.generic.auto._
import music.metadata.api.http.model.{ArtistAliasesRequestBody, AppErrorResponse}
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

object ArtistMetadataApi {

  def routes[F[_]: Concurrent](): HttpRoutes[F] = {

    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case req @ PATCH -> Root / "artist" / id =>
        for {
          maybeNewArtistAliasRequest <- req.as[ArtistAliasesRequestBody].attemptT.leftMap { failure => AppErrorResponse(failure.getCause.getMessage)}.value
          result: Response[F] <- maybeNewArtistAliasRequest.fold(error => BadRequest(AppErrorResponse(error.message)), Ok(_))
          _ = println(id)
        } yield result
    }
  }
}