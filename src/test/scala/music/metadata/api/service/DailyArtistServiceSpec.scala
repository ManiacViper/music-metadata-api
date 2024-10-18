package music.metadata.api.service

import music.metadata.api.domain.Artist
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDate
import java.util.UUID

class DailyArtistServiceSpec extends AnyWordSpec with Matchers {

  "DailyArtistService.getDailyArtist" should {
    "return the same daily artist" when {
      "passed date is the current day" in {
        val service = DailyArtistService.impl()
        val artistOne = Artist(UUID.randomUUID(), "some-name-1", Seq.empty)
        val artistTwo = Artist(UUID.randomUUID(), "some-name-1", Seq.empty)
        val artists = Vector(
          artistOne,
          artistTwo
        )
        val currentDate = LocalDate.now()

        val Some(result) = service.getDailyArtist(artists, currentDate)
        val Some(result2) = service.getDailyArtist(artists, currentDate)

        val _ = result mustBe artistOne
        result2 mustBe artistOne
      }
    }

    "return a different daily artist per day" when {
      "passed date is the current day and for the 2nd call passed date is the next day" in {
        val service = DailyArtistService.impl()
        val artistOne = Artist(UUID.randomUUID(), "some-name-1", Seq.empty)
        val artistTwo = Artist(UUID.randomUUID(), "some-name-2", Seq.empty)
        val artists = Vector(
          artistOne,
          artistTwo
        )
        val currentDate = LocalDate.now()

        val Some(result) = service.getDailyArtist(artists, currentDate)
        val Some(result2) = service.getDailyArtist(artists, currentDate.plusDays(1))
        val Some(result3) = service.getDailyArtist(artists, currentDate.plusDays(2))

        val _ = result mustBe artistOne
        val _ = result2 mustBe artistTwo
        result3 mustBe artistOne //resets to the first one as result 3 is passed day 3 as the date
      }
    }
  }

}
