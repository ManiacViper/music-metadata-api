package music.metadata.api

import cats.effect.Concurrent
import cats.syntax.applicativeError._
import org.http4s.{HttpRoutes, Response}
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.circe.generic.auto._
import music.metadata.api.http.model.{ArtistAliasesRequestBody, DataNotFound, DecodingError, NonFatalError, UnexpectedError}
import music.metadata.api.service.ArtistService
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

import java.util.UUID

object ArtistMetadataApi {

  def routes[F[_]: Concurrent](service: ArtistService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._

    def handleErrorCodes(error: NonFatalError): F[Response[F]] = error match {
      case error: DataNotFound =>
        NotFound(error)
      case error: UnexpectedError =>
        InternalServerError(error)
    }

    def handleSuccess(id: UUID, body: ArtistAliasesRequestBody): F[Response[F]] = for {
      savedAliasesResult <- service.handleAddingAliases(id, body.aliases)
      response: Response[F] <- savedAliasesResult.fold(handleErrorCodes, Ok(_))
    } yield response

    HttpRoutes.of[F] {
      case req @ PATCH -> Root / "artist" / UUIDVar(id) =>
        for {
          maybeNewArtistAliasRequest <- req.as[ArtistAliasesRequestBody].attemptT.leftMap { failure => DecodingError(failure.getCause.getMessage)}.value
          result: Response[F] <- maybeNewArtistAliasRequest.fold(error => BadRequest(error), handleSuccess(id, _))
        } yield result
    }
  }

}