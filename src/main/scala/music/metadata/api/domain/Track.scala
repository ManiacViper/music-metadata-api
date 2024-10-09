package music.metadata.api.domain

import java.util.UUID

/*
for some models im using a universal model in the prototype, ideally they would be seperated into
- http model,
- domain
- and repository
 */

//assuming there should be a limited set of Genres, if it was unlimited then maybe genre could be a string
sealed trait Genre
object Genre {
  def parse(value: String): Either[String, Genre] = value match {
    case "Rock" => Right(Rock)
    case "Disco" => Right(Disco)
    case "Hiphop" => Right(Hiphop)
    case _ => Left("genre not supported")
  }
}
case object Rock extends Genre
case object Disco extends Genre
case object Hiphop extends Genre
final case class Track(id: UUID, title: String, genre: Genre, lengthInSeconds: Long, artistId: UUID)
//assuming there is only 1 primary artist for each track for now, there might be collaborators also which maybe a new field in the future
