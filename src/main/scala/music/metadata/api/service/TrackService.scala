package music.metadata.api.service

import music.metadata.api.domain.Track
import music.metadata.api.repository.TrackRepository

import java.util.UUID

trait TrackService[F[_]] {

  def getTracks(artistId: UUID): F[Seq[Track]]
}

object TrackService {
  def impl[F[_]](trackRepository: TrackRepository[F]) = new TrackService[F] {
    override def getTracks(artistId: UUID): F[Seq[Track]] =
      trackRepository.get(artistId)
  }
}
