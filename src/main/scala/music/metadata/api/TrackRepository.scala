package music.metadata.api

import cats.Applicative
import cats.syntax.applicative._
import music.metadata.api.domain.Track


trait TrackRepository[F[_]]{
  def create(newTrack: Track): F[Unit]
}

object TrackRepository {

  def impl[F[_]: Applicative]: TrackRepository[F] = new TrackRepository[F]{
    def create(newTrack: Track): F[Unit] =
        ().pure[F]
  }
}
