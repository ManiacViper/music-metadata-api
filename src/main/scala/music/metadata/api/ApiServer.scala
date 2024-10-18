package music.metadata.api

import cats.effect.Async
import com.comcast.ip4s._
import fs2.io.net.Network
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger
import cats.syntax.semigroupk._
import music.metadata.api.repository.{ArtistRepository, TrackRepository}
import music.metadata.api.service.ArtistService

object ApiServer {

  def run[F[_]: Async: Network]: F[Nothing] = {
    val trackService = TrackRepository.impl[F]
    val repository = ArtistRepository.impl[F]
    val artistService = ArtistService.impl[F](repository)
    val httpApp = (TrackMetadataApi.routes[F](trackService) <+> ArtistMetadataApi.routes[F](artistService)).orNotFound
    val finalHttpApp = Logger.httpApp(true, true)(httpApp)

    for {
      _ <-
        EmberServerBuilder.default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
  }.useForever
}
