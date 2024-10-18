package music.metadata.api.repository

import cats.effect.Sync
import music.metadata.api.domain.Track

import java.util.UUID
import scala.collection.concurrent.TrieMap


trait TrackRepository[F[_]]{
  def create(newTrack: Track): F[UUID]
  def get(newTrack: UUID): F[Option[Track]]
}

object TrackRepository {
  private val trackMap: TrieMap[UUID, Track] = TrieMap.empty

  def impl[F[_]: Sync]: TrackRepository[F] = new TrackRepository[F]{
    def create(newTrack: Track): F[UUID] = Sync[F].delay {
      trackMap.update(newTrack.id, newTrack)
      newTrack.id
    }

    override def get(newTrack: UUID): F[Option[Track]] =
      Sync[F].delay(trackMap.get(newTrack))
  }
}
