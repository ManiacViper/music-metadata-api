package music.metadata.api

import cats.effect.Concurrent
import cats.syntax.applicativeError._
import org.http4s.{HttpRoutes, Response}
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.circe.generic.auto._
import music.metadata.api.http.model.{ArtistAliasesRequestBody, ArtistResponse, DataInvalid, DataNotFound, DecodingError, NonFatalError, UnexpectedError}
import music.metadata.api.service.ArtistService
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

import java.time.LocalDate
import java.util.UUID
import scala.util.{Failure, Success, Try}

object ArtistMetadataApi {

  def routes[F[_]: Concurrent](service: ArtistService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._

    def handleErrorCodes(error: NonFatalError): F[Response[F]] = error match {
      case error: DataNotFound =>
        NotFound(error)
      case error: DataInvalid =>
        BadRequest(error)
      case error: UnexpectedError =>
        InternalServerError(error)
    }

    def handleDecodeBodySuccess(id: UUID, body: ArtistAliasesRequestBody): F[Response[F]] = for {
      savedAliasesResult <- service.handleAddingAliases(id, body.aliases)
      response: Response[F] <- savedAliasesResult.fold(handleErrorCodes, Ok(_))
    } yield response

    HttpRoutes.of[F] {
      case req @ PATCH -> Root / "artist" / UUIDVar(id) =>
        for {
          maybeNewArtistAliasRequest <- req.as[ArtistAliasesRequestBody].attemptT.leftMap { failure => DecodingError(failure.getCause.getMessage)}.value
          result: Response[F] <- maybeNewArtistAliasRequest.fold(error => BadRequest(error), handleDecodeBodySuccess(id, _))
        } yield result

      case GET -> Root / "artist" / "daily" / datePathParam =>
        Try(LocalDate.parse(datePathParam)) match {
          case Success(date) =>
          service.getDailyArtist(date).flatMap {
            case Right(artist) =>
              Ok(ArtistResponse(artist.id, artist.name, artist.aliases))
            case Left(error) =>
              handleErrorCodes(error)
          }
          case Failure(_) =>
            BadRequest(DecodingError(s"[date=$datePathParam] is invalid format"))
        }
    }
  }

}