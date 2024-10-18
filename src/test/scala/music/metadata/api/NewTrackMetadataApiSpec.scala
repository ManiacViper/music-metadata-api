package music.metadata.api

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe.Json
import org.http4s._
import org.http4s.implicits._
import music.metadata.api.domain.{Hiphop, Track}
import music.metadata.api.http.model.NewTrackRequest
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec._
import io.circe.parser.parse
import io.circe.syntax.EncoderOps
import music.metadata.api.NewTrackMetadataApiSpec._
import music.metadata.api.repository.{ArtistRepository, TrackRepository}
import music.metadata.api.service.TrackService
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.UUID
import scala.concurrent.duration._

class NewTrackMetadataApiSpec extends AnyWordSpec with Matchers {

  "TrackMetadataApi.newTrack route" should {
    "return a 201" when {
      "a new track is created" in {
        val newTrack: NewTrackRequest = NewTrackRequest("some title", Hiphop.toString, (2.minutes + 45.seconds).toSeconds, UUID.randomUUID())

        val result: Response[IO] = newTrackRoute(newTrack.asJson)

        val _ = result.status mustBe Status.Created
        isValidTrackId(result) mustBe true
      }
    }

    "return a 400" when {
      "genre is not supported" in {
        val unsupportedGenre = "Jazz"
        val newTrack: NewTrackRequest =
          NewTrackRequest("some title", unsupportedGenre, (2.minutes + 45.seconds).toSeconds, UUID.randomUUID())

        val result: Response[IO] = newTrackRoute(newTrack.asJson)

        val _ = result.status mustBe Status.BadRequest
        val resultBody = result.as[Json].unsafeRunSync()
        val Right(expectedMsg) = parse(
          """
            |{
            |  "message" : "genre not supported"
            |}
            |""".stripMargin)
        resultBody mustBe expectedMsg
      }

      "artist id is invalid format" in {
        val invalidArtistId = "invalid-artist-id"
        val newTrack: TestNewTrackRequest =
          TestNewTrackRequest("some title", Hiphop.toString, (2.minutes + 45.seconds).toSeconds, invalidArtistId)

        val result: Response[IO] = newTrackRoute(newTrack.asJson)

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

    "return 500" when {
      "there is an unexpected problem saving new track" in {
        val newTrack: NewTrackRequest = NewTrackRequest("some title", Hiphop.toString, (2.minutes + 45.seconds).toSeconds, UUID.randomUUID())

        val result: Response[IO] = failedNewTrackRoute(newTrack.asJson)

        val _ = result.status mustBe Status.InternalServerError
        val resultBody = result.as[Json].unsafeRunSync()
        val Right(expectedMsg) = parse(
          """
            |{
            |  "message" : "new track could not be saved"
            |}
            |""".stripMargin)
        resultBody mustBe expectedMsg
      }
    }
  }
}

object NewTrackMetadataApiSpec {

  def failedNewTrackRoute(newTrackReq: Json): Response[IO] = {
    val newTrackRequest = Request[IO](Method.POST, uri"/newtrack").withEntity(newTrackReq)
    val trackRepository = new TrackRepository[IO] {
      override def create(newTrack: Track): IO[UUID] =
        IO.raiseError(new RuntimeException("something went wrong"))
      override def get(artistId: UUID): IO[Seq[Track]] = ???
    }
    val service = TrackService.impl(trackRepository)

    TrackMetadataApi.routes(trackRepository, service).orNotFound(newTrackRequest)
  }.unsafeRunSync()

  def newTrackRoute(newTrackReq: Json): Response[IO] = {
    println(newTrackReq.spaces2)
    val newTrackRequest = Request[IO](Method.POST, uri"/newtrack").withEntity(newTrackReq)
    val trackRepository = TrackRepository.impl[IO](ArtistRepository.existingArtists.map(_.id))
    val service = TrackService.impl(trackRepository)
    TrackMetadataApi.routes(trackRepository, service).orNotFound(newTrackRequest)
  }.unsafeRunSync()

  final case class TestNewTrackRequest(title: String, genre: String, lengthInSeconds: Long, artistId: String)

  private def isValidTrackId(resp: Response[IO]): Boolean = {
    resp
      .as[Json]
      .map(_.hcursor.get[String]("newTrackId").toTry)
      .map(_.isSuccess)
  }.unsafeRunSync()
}