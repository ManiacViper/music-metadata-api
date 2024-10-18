package music.metadata.api.service

import music.metadata.api.domain.Artist
import music.metadata.api.http.model.{DataInvalid, DataNotFound}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.{LocalDate, ZoneOffset}
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
        val currentDate = LocalDate.now(ZoneOffset.UTC)

        val Right(result) = service.getDailyArtist(artists, currentDate)
        val Right(result2) = service.getDailyArtist(artists, currentDate)

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
        val currentDate = LocalDate.now(ZoneOffset.UTC)

        val Right(result) = service.getDailyArtist(artists, currentDate)
        val Right(result2) = service.getDailyArtist(artists, currentDate.plusDays(1))
        val Right(result3) = service.getDailyArtist(artists, currentDate.plusDays(2))

        val _ = result mustBe artistOne
        val _ = result2 mustBe artistTwo
        result3 mustBe artistOne //resets to the first one as result 3 is passed day 3 as the date
      }
    }

    "return error" when {
      "date passed is in the past" in {
        val artistOne = Artist(UUID.randomUUID(), "some-name-1", Seq.empty)
        val artistTwo = Artist(UUID.randomUUID(), "some-name-2", Seq.empty)
        val artists = Vector(
          artistOne,
          artistTwo
        )
        val service = DailyArtistService.impl()
        val previousDate = LocalDate.now().minusDays(1)

        val Left(result) = service.getDailyArtist(artists, date = previousDate)
        result mustBe DataInvalid(s"[date=$previousDate] cannot be in the past")
      }
    }

    "return no daily artist" when {
      "given no artists" in {
        val service = DailyArtistService.impl()
        val result = service.getDailyArtist(artists = Vector.empty, date = LocalDate.now())
        result mustBe Left(DataNotFound("artist not found"))
      }
    }
  }

}
