package music.metadata.api.http.model


import io.circe.Decoder
import music.metadata.api.domain.{Genre, Track}

import java.util.UUID

final case class NewTrackResponse(newTrackId: UUID)
final case class NewTrackErrorResponse(message: String)

final case class NewTrackRequest(title: String, genre: String, lengthInSeconds: Long, artistId: UUID)
object NewTrackRequest {
  implicit val decodeUUID: Decoder[UUID] =
    Decoder.decodeUUID.withErrorMessage("uuid format is invalid")

  def toNewTrack(newTrackRequest: NewTrackRequest): Either[String, Track] =
    for {
      genre <- Genre.parse(newTrackRequest.genre)
      track =  Track(
        id = UUID.randomUUID(), //this unique id could be handled by the future db that will be used as a primary key for track
        title = newTrackRequest.title,
        genre = genre,
        lengthInSeconds = newTrackRequest.lengthInSeconds,
        artistId = newTrackRequest.artistId
      )
    } yield track

}
