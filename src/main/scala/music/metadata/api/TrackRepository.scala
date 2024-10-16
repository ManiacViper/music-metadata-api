package music.metadata.api

import cats.Applicative
import cats.syntax.applicative._
import music.metadata.api.domain.Track

import java.util.UUID
import scala.collection.concurrent.TrieMap


trait TrackRepository[F[_]]{
  def create(newTrack: Track): F[UUID]
  def get(newTrack: UUID): F[Option[Track]]
}

object TrackRepository {
  private val trackMap: TrieMap[UUID, Track] = TrieMap.empty

  def impl[F[_]: Applicative]: TrackRepository[F] = new TrackRepository[F]{
    def create(newTrack: Track): F[UUID] = {
      trackMap.update(newTrack.id, newTrack)
      (newTrack.id).pure[F]
    }

    override def get(newTrack: UUID): F[Option[Track]] =
      trackMap.get(newTrack).pure[F]
  }
}
