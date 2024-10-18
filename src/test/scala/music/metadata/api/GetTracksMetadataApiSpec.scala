
package music.metadata.api

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe.Json
//import io.circe.generic.auto._
import io.circe.parser.parse
//import io.circe.syntax.EncoderOps
import music.metadata.api.domain.Track
//import music.metadata.api.http.model.NewTrackRequest
import music.metadata.api.repository.TrackRepository
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.implicits._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.UUID
//import scala.concurrent.duration._

class GetTracksMetadataApiSpec extends AnyWordSpec with Matchers {

  "TrackMetadataApi.routes" should {
    "return a 201" when {
      "a new track is created" in {
        val result: Response[IO] = getTracksRoute(UUID.randomUUID())

        val _ = result.status mustBe Status.Ok
//        val actual = parse(result.as[String].unsafeRunSync())
//        actual mustBe parse(
//          """
//            |{
//            |   "artistId": $artistId,
//            |   "tracks": [
//            |     {},
//            |     {}
//            |   ]
//            |}
//            |""".stripMargin)
      }
    }

    "return a 400" ignore {
      "artist id is invalid format" in {
        val _ = "invalid-artist-id"

        val result: Response[IO] = getTracksRoute(UUID.randomUUID())

        val _ = result.status mustBe Status.BadRequest
        val resultBody = result.as[Json].unsafeRunSync()
        val Right(expectedMsg) = parse(
          """
            |{
            |  "message" : "DecodingFailure at .artistId: Got value '\"invalid-artist-id\"' with wrong type, expecting string"
            |}
            |""".stripMargin)
        resultBody mustBe expectedMsg
      }
    }

    "return 500" ignore {
      "there is an unexpected problem returning tracks" in {

        val result: Response[IO] = failedGetTracksRoute(UUID.randomUUID())

        val _ = result.status mustBe Status.InternalServerError
        val resultBody = result.as[Json].unsafeRunSync()
        val Right(expectedMsg) = parse(
          """
            |{
            |  "message" : "tracks "
            |}
            |""".stripMargin)
        resultBody mustBe expectedMsg
      }
    }
  }

  private[this] def failedGetTracksRoute(artistId: UUID): Response[IO] = {
    val newTrackRequest = Request[IO](Method.GET, uri"/tracks"/ artistId)
    val failedTrackRepository = new TrackRepository[IO] {
      override def create(newTrack: Track): IO[UUID] =
        IO.raiseError(new RuntimeException("something went wrong"))
      override def get(newTrack: UUID): IO[Option[Track]] = ???
    }
    TrackMetadataApi.routes(failedTrackRepository).orNotFound(newTrackRequest)
  }.unsafeRunSync()

  private[this] def getTracksRoute(artistId: UUID): Response[IO] = {
    val newTrackRequest = Request[IO](Method.GET, uri"/tracks"/artistId)
    val trackService = TrackRepository.impl[IO]
    TrackMetadataApi.routes(trackService).orNotFound(newTrackRequest)
  }.unsafeRunSync()
}

