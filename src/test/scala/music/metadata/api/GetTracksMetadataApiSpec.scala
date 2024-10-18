
package music.metadata.api

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe.Json
import music.metadata.api.domain.{Disco, Hiphop, Track}
import music.metadata.api.http.model.{NewTrackRequest, NewTrackResponse}
import music.metadata.api.repository.ArtistRepository
import music.metadata.api.service.TrackService
import io.circe.generic.auto._
import io.circe.parser.parse
import io.circe.syntax._
import music.metadata.api.repository.TrackRepository
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.implicits._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.UUID

class GetTracksMetadataApiSpec extends AnyWordSpec with Matchers {

  "TrackMetadataApi.tracks route" should {
    "return a 200 and the tracks" when {
      "an artist has tracks" in {
        implicit val repository: TrackRepository[IO] = TrackRepository.impl[IO](ArtistRepository.existingArtists.map(_.id))
        val artistId = UUID.fromString("5457804f-f9df-47e1-bc2b-250dceef9093")
        val tracks = Seq(
          NewTrackRequest("some-title-1", Hiphop.toString, 120, artistId),
          NewTrackRequest("some-title-2", Disco.toString, 240, artistId)
        )
        val newTrackResponses: Seq[NewTrackResponse] =
          tracks.map(track => newTrackRoute(track.asJson).as[NewTrackResponse].unsafeRunSync())

        val result: Response[IO] = getTracksRoute(artistId)

        val _ = result.status mustBe Status.Ok
        val actual = result.as[Json].unsafeRunSync()
        val Right(expected) =
          parse(
          s"""
             |{
             |   "artistId": "$artistId",
             |   "tracks": [
             |    {
             |      "id" : "${newTrackResponses.head.newTrackId}",
             |      "title" : "some-title-1",
             |      "genre" : "Hiphop",
             |      "lengthInSeconds" : 120
             |    },
             |    {
             |      "id" : "${newTrackResponses(1).newTrackId}",
             |      "title" : "some-title-2",
             |      "genre" : "Disco",
             |      "lengthInSeconds" : 240
             |    }
             |   ]
             |}
             |""".stripMargin)
        actual mustBe expected
      }
    }

    "return 404 or not found" when {
      "artist has no tracks" in {
        implicit val repository: TrackRepository[IO] = TrackRepository.impl[IO](ArtistRepository.existingArtists.map(_.id))
        val artistId = UUID.fromString("5457804f-f9df-47e1-bc2b-250dceef9093")
        val result: Response[IO] = getTracksRoute(artistId)

        val _ = result.status mustBe Status.NotFound
        val actual = result.as[Json].unsafeRunSync()
        val Right(expected) = parse(s"""
                                       |{
                                       |   "artistId": "$artistId",
                                       |   "tracks": []
                                       |}
                                       |""".stripMargin)
        actual mustBe expected
      }
    }

    "return a 400" ignore {
      implicit val repository: TrackRepository[IO] = TrackRepository.impl[IO](ArtistRepository.existingArtists.map(_.id))
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
      override def get(artistId: UUID): IO[Seq[Track]] = ???
    }
    val service = TrackService.impl(failedTrackRepository)
    TrackMetadataApi.routes(failedTrackRepository, service).orNotFound(newTrackRequest)
  }.unsafeRunSync()

  private[this] def getTracksRoute(artistId: UUID)(implicit repository: TrackRepository[IO]): Response[IO] = {
    val newTrackRequest = Request[IO](Method.GET, uri"/tracks"/artistId)
    val service = TrackService.impl(repository)
    TrackMetadataApi.routes(repository, service).orNotFound(newTrackRequest)
  }.unsafeRunSync()

  private[this] def newTrackRoute(newTrackReq: Json)(implicit repository: TrackRepository[IO]): Response[IO] = {
    val newTrackRequest = Request[IO](Method.POST, uri"/newtrack").withEntity(newTrackReq)
    val service = TrackService.impl(repository)
    TrackMetadataApi.routes(repository, service).orNotFound(newTrackRequest)
  }.unsafeRunSync()
}

