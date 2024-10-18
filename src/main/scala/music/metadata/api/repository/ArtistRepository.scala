package music.metadata.api.repository

import cats.effect.Sync
import music.metadata.api.domain.Artist

import java.util.UUID
import scala.collection.concurrent.TrieMap

trait ArtistRepository[F[_]] {
  def addAliases(id: UUID, newAliases: Seq[String]): F[Option[Artist]]
}

object ArtistRepository {

  private val artist1: Artist = Artist(UUID.fromString("5457804f-f9df-47e1-bc2b-250dceef9093"), "some-artist-1", Seq("alias-1", "alias-2"))
  private val artist2: Artist = Artist(UUID.fromString("5fa4b21d-3dd2-4d0d-aeac-85faed9b2f95"), "some-artist-2", Seq("some-alias"))
  private val artist3: Artist = Artist(UUID.fromString("916e2cff-a76a-45f5-b373-c49d1c46828f"), "some-artist-3", Seq.empty[String])
  val existingArtists = Seq(artist1,artist2,artist3)

  def impl[F[_]: Sync](existingArtistIds: Seq[Artist]): ArtistRepository[F] = new ArtistRepository[F] {

    private val artistMap: TrieMap[UUID, Artist] =
      existingArtistIds
        .foldLeft(TrieMap.empty[UUID, Artist]) { case (trieMapAcc, artist) => trieMapAcc.update(artist.id,artist); trieMapAcc}

    def addAliases(id: UUID, newAliases: Seq[String]): F[Option[Artist]] = Sync[F].delay {
      for {
        artist <- artistMap.get(id)
        updatedArtist = artist.copy(aliases = artist.aliases ++ newAliases)
        _ = artistMap.update(id, updatedArtist)
      } yield updatedArtist
    }
  }

}