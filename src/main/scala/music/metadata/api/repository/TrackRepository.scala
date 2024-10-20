package music.metadata.api.repository

import cats.effect.Sync
import music.metadata.api.domain.Track

import java.util.UUID
import scala.collection.concurrent.TrieMap
import cats.syntax.flatMap._

trait TrackRepository[F[_]]{
  def create(newTrack: Track): F[UUID]
  def get(artistId: UUID): F[Seq[Track]]
  def doesArtistExist(artistId: UUID): F[Boolean]
}

object TrackRepository {

  //maybe track repository should take in and know about artist repository
  def impl[F[_]: Sync](storedArtistIds: Seq[UUID]): TrackRepository[F] = new TrackRepository[F] {

    private val tracksStore: TrieMap[UUID, Vector[Track]] = storedArtistIds.foldLeft(TrieMap.empty[UUID, Vector[Track]]) { case (acc, artistId) =>
      acc.update(artistId, Vector.empty)
      acc
    }

    def create(newTrack: Track): F[UUID] =
      Sync[F].delay {
        tracksStore.updateWith(newTrack.artistId) {
          case Some(existingTracks: Vector[Track]) => Some(existingTracks :+ newTrack)
          case None => Some(Vector(newTrack))
        }
      }.flatMap {
        case Some(_) => Sync[F].pure(newTrack.id)
        //TODO: test None
        case None => Sync[F].raiseError(new RuntimeException(s"could not store track [id=${newTrack.id}]"))
      }

    override def get(artistId: UUID): F[Seq[Track]] =
      Sync[F].delay(
        tracksStore
          .get(artistId)
          .fold(Vector.empty[Track])(identity)
      )

    override def doesArtistExist(artistId: UUID): F[Boolean] =
      Sync[F].delay(
        tracksStore
          .get(artistId)
          .fold(false)(_ => true)
      )
  }
}
