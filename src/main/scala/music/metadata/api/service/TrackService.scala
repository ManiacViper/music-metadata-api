package music.metadata.api.service

import cats.effect.Sync
import music.metadata.api.domain.Track
import music.metadata.api.repository.TrackRepository
import cats.syntax.functor._
import cats.syntax.applicativeError._

import java.util.UUID

trait TrackService[F[_]] {

  def saveNewTrack(newTrack: Track): F[Unit]
  def getTracks(artistId: UUID): F[Seq[Track]]
}
//TODO: tests for this class
object TrackService {
  def impl[F[_]: Sync](trackRepository: TrackRepository[F]): TrackService[F] = new TrackService[F] {
    override def getTracks(artistId: UUID): F[Seq[Track]] =
      trackRepository.get(artistId)

    //TODO: an artist id check when creating a new track - DataNotFound
    override def saveNewTrack(newTrack: Track): F[Unit] =
      trackRepository
        .create(newTrack)
        .map(_ => ())
        .handleErrorWith { _ =>
          Sync[F].raiseError(new RuntimeException("new track could not be saved"))
        }
  }
}
