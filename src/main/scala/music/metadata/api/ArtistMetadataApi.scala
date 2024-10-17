package music.metadata.api

import cats.effect.Concurrent
import cats.syntax.applicativeError._
import org.http4s.{HttpRoutes, Response}
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.circe.generic.auto._
import music.metadata.api.domain.Artist
import music.metadata.api.http.model.{AppErrorResponse, ArtistAliasesRequestBody}
import music.metadata.api.repository.ArtistRepository
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

import java.util.UUID

object ArtistMetadataApi {

  def routes[F[_]: Concurrent](repository: ArtistRepository[F]): HttpRoutes[F] = {

    val dsl = new Http4sDsl[F]{}
    import dsl._

    def handleSuccess(id: UUID, body: ArtistAliasesRequestBody) = (for {
      maybeSavedAliases: Option[Artist] <- repository.addAliases(id, body.aliases)
      response = maybeSavedAliases.fold(NotFound(AppErrorResponse(s"artist [id=${id}] not found")))(Ok(_))
    } yield response).flatten

    HttpRoutes.of[F] {
      case req @ PATCH -> Root / "artist" / UUIDVar(id) =>
        for {
          maybeNewArtistAliasRequest <- req.as[ArtistAliasesRequestBody].attemptT.leftMap { failure => AppErrorResponse(failure.getCause.getMessage)}.value
          result: Response[F] <- maybeNewArtistAliasRequest.fold(error => BadRequest(AppErrorResponse(error.message)), handleSuccess(id, _))
          _ = println(id)
        } yield result
    }
  }

}