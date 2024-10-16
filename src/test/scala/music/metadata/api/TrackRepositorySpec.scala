package music.metadata.api
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import music.metadata.api.domain.{Hiphop, Track}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.UUID

class TrackRepositorySpec extends AnyWordSpec with Matchers {

  "TrackRepository.create and .get" should {
    "store a track and return a track" when {
      "new track is passed and we can retrieve it" in {
        val newTrack = Track(UUID.randomUUID(),"some-title", Hiphop, 100, UUID.randomUUID())
        val repository = TrackRepository.impl[IO]

        val createResult = repository.create(newTrack).unsafeRunSync()
        val Some(getResult) = repository.get(newTrack.id).unsafeRunSync()

        val _ = createResult mustBe newTrack.id
        getResult mustBe newTrack
      }
    }

    "not retrieve a track" when {
      "a track is not stored for an id" in {
        val newTrack = Track(UUID.randomUUID(),"some-title", Hiphop, 100, UUID.randomUUID())
        val repository = TrackRepository.impl[IO]

        val getResult = repository.get(newTrack.id).unsafeRunSync()

        getResult mustBe None
      }
    }
  }

}
