package music.metadata.api.service

import cats.MonadError
import cats.data.EitherT
import music.metadata.api.domain.Artist
import music.metadata.api.http.model.{DataNotFound, NonFatalError, UnexpectedError}
import music.metadata.api.repository.ArtistRepository
import cats.syntax.applicativeError._

import java.util.UUID

trait ArtistService[F[_]] {
  def handleAddingAliases(id: UUID, aliases: Seq[String]): F[Either[NonFatalError, Artist]]
}

object ArtistService {
  def impl[F[_]](repository: ArtistRepository[F])(implicit ME: MonadError[F, Throwable]): ArtistService[F] = new ArtistService[F] {
    def handleAddingAliases(id: UUID, newAliases: Seq[String]): F[Either[NonFatalError, Artist]] =
      (for {
        maybeAdded <- repository.addAliases(id, newAliases).attemptT.leftMap { _ =>
                        UnexpectedError(s"could not save aliases for artist [id=$id]")
                     }
        result <- EitherT.fromEither(maybeAdded.toRight[NonFatalError](DataNotFound(s"artist [id=${id}] not found")))
      } yield result).value
  }
}