package music.metadata.api.http.model


import cats.effect.Concurrent
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import music.metadata.api.domain.{Genre, Track}
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

import java.util.UUID
import scala.util.Try

final case class NewTrackResponse(newTrackId: UUID)
final case class NewTrackErrorResponse(message: String)

final case class NewTrackRequest(title: String, genre: String, lengthInSeconds: Long, artist: UUID)
object NewTrackRequest {

  implicit val uuidEncoder: Encoder[UUID] = Encoder.encodeString.contramap[UUID](_.toString)
  implicit val uuidDecoder: Decoder[UUID] = Decoder.decodeString.emap(str =>
    Try(UUID.fromString(str)).toEither.left.map(_ => s"not a valid uuid format")
  )
  implicit def entityDecoder[F[_]: Concurrent]: EntityDecoder[F, NewTrackRequest] =
    jsonOf[F, NewTrackRequest]

  def toNewTrack(newTrackRequest: NewTrackRequest): Either[String, Track] =
    for {
      genre <- Genre.parse(newTrackRequest.genre)
      track =  Track(
        id = UUID.randomUUID(), //this id could be handled by the future db that will be used as a primary key indexed usually
        title = newTrackRequest.title,
        genre = genre,
        lengthInSeconds = newTrackRequest.lengthInSeconds,
        artist = newTrackRequest.artist
      )
    } yield track

}
