package music.metadata.api

import cats.effect.IO
import io.circe.Json
import music.metadata.api.http.model.ArtistAliasesRequestBody
import org.http4s.{Method, Request, Response, Status}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.http4s.implicits._
import cats.effect.unsafe.implicits.global
import io.circe.parser.parse
import io.circe.syntax._
import io.circe.generic.auto._
import music.metadata.api.repository.ArtistRepository
import org.http4s.circe.CirceEntityCodec._

import java.util.UUID

class ArtistMetadataApiSpec extends AnyWordSpec with Matchers {


  "ArtistMetadataApi.routes" should {
    "add artist aliases" when {
      "one or more artist aliases are provided" in {
        val existingArtistId = UUID.fromString("916e2cff-a76a-45f5-b373-c49d1c46828f")
        val newArtistAliases = ArtistAliasesRequestBody(aliases = Seq("zorro", "eminem"))
        val result: Response[IO] = newArtistAliasesRoute(existingArtistId, newArtistAliases.asJson)
        val _ = result.status mustBe Status.Ok

        val moreArtistAliases = ArtistAliasesRequestBody(aliases = Seq("missy", "jacky"))
        val resultWithMore: Response[IO] = newArtistAliasesRoute(existingArtistId, moreArtistAliases.asJson)
        val actual = resultWithMore.as[Json].unsafeRunSync()
        val _ = resultWithMore.status mustBe Status.Ok

        val Right(expected) = parse(
                                  """
                                    |{
                                    | "id": "916e2cff-a76a-45f5-b373-c49d1c46828f",
                                    | "name": "some-artist-3",
                                    | "aliases": ["zorro","eminem","missy","jacky"]
                                    |}
                                    |""".stripMargin)
        actual mustBe expected
      }
    }

    "return artist not found" when {
      "artist does not exist" in {
        val nonExistingArtistId = UUID.randomUUID()
        val moreArtistAliases = ArtistAliasesRequestBody(aliases = Seq("missy", "jacky"))
        val resultWithMore: Response[IO] = newArtistAliasesRoute(nonExistingArtistId, moreArtistAliases.asJson)

        val actual = resultWithMore.as[Json].unsafeRunSync()

        val _ = resultWithMore.status mustBe Status.NotFound
        val Right(expected) = parse(
          s"""
            |{
            | "message" : "artist [id=${nonExistingArtistId}] not found"
            |}
            |""".stripMargin)
        actual mustBe expected
      }
    }

  }

  private[this] def newArtistAliasesRoute(id: UUID, newArtistAliases: Json): Response[IO] = {
    val newAliasesRequest = Request[IO](Method.PATCH, uri"/artist"/id.toString).withEntity(newArtistAliases)
    ArtistMetadataApi.routes[IO](ArtistRepository.impl[IO]).orNotFound(newAliasesRequest)
  }.unsafeRunSync()

}
