package music.metadata.api

import cats.Applicative
import cats.syntax.applicative._
import music.metadata.api.domain.Track


trait TrackService[F[_]]{
  def create(newTrack: Track): F[Unit]
}

object TrackService {

  def impl[F[_]: Applicative]: TrackService[F] = new TrackService[F]{
    def create(newTrack: Track): F[Unit] =
        ().pure[F]
  }
}
