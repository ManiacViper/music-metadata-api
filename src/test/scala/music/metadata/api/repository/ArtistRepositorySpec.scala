package music.metadata.api.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import music.metadata.api.domain.Artist
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.UUID

class ArtistRepositorySpec extends AnyWordSpec with Matchers {

  "ArtistRepository.addAliases" should {
    "add new aliases" when {
      "artist exists" in {
        val id = UUID.fromString("5457804f-f9df-47e1-bc2b-250dceef9093")
        val aliases = Seq("some-new-alias", "some-new-alias-2")
        val repository = ArtistRepository.impl[IO]

        val Some(result) = repository.addAliases(id, aliases).unsafeRunSync()

        result mustBe Artist(id, "some-artist-1", Seq("alias-1", "alias-2", "some-new-alias", "some-new-alias-2"))
      }
    }

    "return none" when {
      "no artist exists with the id passed" in {
        val nonExistentArtistId = UUID.randomUUID()
        val aliases = Seq("some-new-alias", "some-new-alias-2")
        val repository = ArtistRepository.impl[IO]

        val result = repository.addAliases(nonExistentArtistId, aliases).unsafeRunSync()

        result mustBe None
      }
    }

    "return error" when {
      "there is an error storing aliases" in {
        val nonExistentArtistId = UUID.randomUUID()
        val aliases = Seq("some-new-alias", "some-new-alias-2")
        val repository = new ArtistRepository[IO] {
          override def addAliases(id: UUID, newAliases: Seq[String]): IO[Option[Artist]] =
            IO.raiseError(new RuntimeException("something went wrong"))
        }

        val result = intercept[RuntimeException](repository.addAliases(nonExistentArtistId, aliases).unsafeRunSync())

        result.getMessage mustBe "something went wrong"
      }
    }
  }

}
