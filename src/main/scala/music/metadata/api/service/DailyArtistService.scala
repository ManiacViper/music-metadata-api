package music.metadata.api.service

import music.metadata.api.domain.Artist

import java.time.{LocalDate, ZoneOffset}

trait DailyArtistService {
  def getDailyArtist(artists: Vector[Artist], date: LocalDate): Option[Artist]
}
object DailyArtistService {
//  private val maxIndex = artists.size - 1

  def impl(): DailyArtistService = new DailyArtistService {
    private var currentIndex = 0
    private var storedDate: LocalDate = LocalDate.now(ZoneOffset.UTC)

    override def getDailyArtist(artists: Vector[Artist], date: LocalDate): Option[Artist] = {
      lazy val maxIndex = artists.size - 1
      if (date == storedDate) {
        artists.lift(currentIndex) //return same artist for the same day
      } else {
        if (currentIndex < maxIndex) {
          currentIndex = currentIndex + 1 //return different artist for the next day
          storedDate = date
          artists.lift(currentIndex)
        } else {
          currentIndex = 0 //reset to first one
          storedDate = date
          artists.lift(currentIndex)
        }
      }
    }

  }
}
