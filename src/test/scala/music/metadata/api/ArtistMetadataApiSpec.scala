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
import org.http4s.circe.CirceEntityCodec._

import java.util.UUID

class ArtistMetadataApiSpec extends AnyWordSpec with Matchers {

  "ArtistMetadataApi.routes" should {
    "add artist aliases" when {
      "one or more artist aliases are provided" in {
        val newArtistAliases = ArtistAliasesRequestBody(aliases = Seq("zorro", "eminem"))

        val result: Response[IO] = newArtistAliasesRoute(newArtistAliases.asJson)
        val actual = result.as[Json].unsafeRunSync()

        val _ = result.status mustBe Status.Ok

        val Right(expected) = parse(
                                  """
                                    |{
                                    | "aliases": ["zorro","eminem"]
                                    |}
                                    |""".stripMargin)
        actual mustBe expected
      }
    }
  }

  private[this] def newArtistAliasesRoute(newArtistAliases: Json): Response[IO] = {

    val id = UUID.randomUUID().toString
    val newAliasesRequest = Request[IO](Method.PATCH, uri"/artist"/id).withEntity(newArtistAliases)
    ArtistMetadataApi.routes[IO]().orNotFound(newAliasesRequest)
  }.unsafeRunSync()

}
