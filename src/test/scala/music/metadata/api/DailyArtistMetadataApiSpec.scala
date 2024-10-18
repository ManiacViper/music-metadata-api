package music.metadata.api

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe.Json
//import io.circe.generic.auto._
import io.circe.parser.parse
//import io.circe.syntax._
import music.metadata.api.domain.Artist
import music.metadata.api.repository.ArtistRepository
import music.metadata.api.service.ArtistService
import org.http4s.circe.CirceEntityCodec._
import org.http4s.implicits._
import org.http4s.{Method, Request, Response, Status}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDate
import java.util.UUID

class DailyArtistMetadataApiSpec extends AnyWordSpec with Matchers {


  "ArtistMetadataApi.routes" should {
    "add artist aliases" when {
      implicit val repository: ArtistRepository[IO] = ArtistRepository.impl[IO](ArtistRepository.existingArtists)
      "one or more artist aliases are provided" in {
        val date = LocalDate.now()
        val resultWithMore: Response[IO] = dailyArtistRoute(date.toString)
        val _ = resultWithMore.as[Json].unsafeRunSync()
        val _ = resultWithMore.status mustBe Status.Ok

//        val Right(expected) = parse(
//                                  """
//                                    |{
//                                    | "id": "5457804f-f9df-47e1-bc2b-250dceef9093",
//                                    | "name": "some-artist-1",
//                                    | "aliases": ["alias-1", "alias-2"]
//                                    |}
//                                    |""".stripMargin)
//        actual mustBe expected
      }
    }

    "return a 400" when {
      implicit val repository: ArtistRepository[IO] = ArtistRepository.impl[IO](ArtistRepository.existingArtists)
      "artist id is invalid format" in {
        val invalidDate = "invalidDate"
        val result: Response[IO] = dailyArtistRoute(invalidDate)

        val _ = result.status mustBe Status.BadRequest
        val resultBody = result.as[Json].unsafeRunSync()
        val Right(expectedMsg) = parse(
          """
            |{
            |  "message" : "[date=invalidDate] is invalid format"
            |}
            |""".stripMargin)
        resultBody mustBe expectedMsg
      }
    }

    "return error" ignore {
      implicit val failedRepository: ArtistRepository[IO] = new ArtistRepository[IO] {
        override def addAliases(id: UUID, newAliases: Seq[String]): IO[Option[Artist]] = ???
        override   def getAllArtists: IO[Seq[Artist]] =
          IO.raiseError(new RuntimeException("something went wrong"))
      }
      "there is an error with saving the new aliases" in {
        val date = LocalDate.now()
        val resultWithMore: Response[IO] = dailyArtistRoute(date.toString)
        val actual = resultWithMore.as[Json].unsafeRunSync()
        val _ = resultWithMore.status mustBe Status.InternalServerError

        val Right(expected) = parse(
          """
            |{
            | "message": ""
            |}
            |""".stripMargin)
        actual mustBe expected
      }
    }
  }

  private[this] def dailyArtistRoute(date: String)(implicit repository: ArtistRepository[IO]): Response[IO] = {
    val newAliasesRequest = Request[IO](Method.GET, uri"/artist/daily"/date)
    val service = ArtistService.impl(repository)
    ArtistMetadataApi.routes(service).orNotFound(newAliasesRequest)
  }.unsafeRunSync()

}
