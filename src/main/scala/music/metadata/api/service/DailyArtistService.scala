package music.metadata.api.service

import music.metadata.api.domain.Artist
import music.metadata.api.http.model.{DataInvalid, DataNotFound, NonFatalError}

import java.time.{LocalDate, ZoneOffset}

trait DailyArtistService {
  def getDailyArtist(artists: Vector[Artist], date: LocalDate): Either[NonFatalError, Artist]
}
object DailyArtistService {
//  private val maxIndex = artists.size - 1

  def impl(): DailyArtistService = new DailyArtistService {
    private var currentIndex = 0
    private var storedDate: LocalDate = LocalDate.now(ZoneOffset.UTC)
    private val artistNotFound = DataNotFound(s"artist not found")

    //this does not pre-compute once when the first request comes in and stores the result, could do and write the computed value in the db or inmemory storage for the current day, was focused on solving the problem
    override def getDailyArtist(artists: Vector[Artist], date: LocalDate): Either[NonFatalError, Artist] = {
      if (date == storedDate) {
        artists.lift(currentIndex).toRight(artistNotFound) //return the same artist for the same day
      } else if(date.isBefore(storedDate)) {
        Left(DataInvalid(s"[date=$date] cannot be in the past"))
      } else {
        val maxIndex = artists.size - 1
        if (currentIndex < maxIndex) {
          currentIndex = currentIndex + 1 //return different artist for a different date
          storedDate = date
          artists.lift(currentIndex).toRight(artistNotFound)
        } else {
          currentIndex = 0 //reset to first one
          storedDate = date
          artists.lift(currentIndex).toRight(artistNotFound)
        }
      }
    }

  }
}
