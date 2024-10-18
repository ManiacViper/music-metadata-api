package music.metadata.api.service

import music.metadata.api.domain.Track

import java.util.UUID

trait ArtistAndTrackService[F[_]] {

  def saveNewTrack(newTrack: Track): F[UUID]
}
