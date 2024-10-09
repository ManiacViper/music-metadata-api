package music.metadata.api

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe.Json
import org.http4s._
import org.http4s.implicits._
import music.metadata.api.domain.Hiphop
import music.metadata.api.http.model.NewTrackRequest
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import io.circe.parser.parse
import music.metadata.api.TrackMetadataApiSpec.isValidTrackId
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.UUID
import scala.concurrent.duration._

class TrackMetadataApiSpec extends AnyWordSpec with Matchers {

  "returns a 201 and the new track id when track is added" in {
    val newTrack: NewTrackRequest = NewTrackRequest("some title", Hiphop.toString, (2.minutes + 45.seconds).toSeconds, UUID.randomUUID())

    val result: Response[IO] = newTrackRoute(newTrack)

    val _ = result.status mustBe Status.Created
    isValidTrackId(result) mustBe true
  }

  private[this] def newTrackRoute(newTrackReq: NewTrackRequest): Response[IO] = {
    val newTrackRequest = Request[IO](Method.POST, uri"/newtrack").withEntity(newTrackReq)
    val trackService = TrackService.impl[IO]
    TrackMetadataApi.routes(trackService).orNotFound(newTrackRequest)
  }.unsafeRunSync()
}

object TrackMetadataApiSpec {

  private def isValidTrackId(resp: Response[IO]): Boolean = {
    resp
      .as[String]
      .map(parse(_).getOrElse(Json.Null))
      .map(_.hcursor.get[String]("newTrackId").toTry)
      .map(_.isSuccess)
  }.unsafeRunSync()
}