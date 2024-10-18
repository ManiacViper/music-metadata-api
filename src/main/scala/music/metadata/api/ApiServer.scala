package music.metadata.api

import cats.effect.Async
import com.comcast.ip4s._
import fs2.io.net.Network
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger
import cats.syntax.semigroupk._
import music.metadata.api.repository.{ArtistRepository, TrackRepository}
import music.metadata.api.service.{ArtistService, TrackService}

object ApiServer {

  def run[F[_]: Async: Network]: F[Nothing] = {
    val trackRepository = TrackRepository.impl[F](ArtistRepository.existingArtists.map(_.id))
    val trackService = TrackService.impl[F](trackRepository)
    val artistRepository = ArtistRepository.impl[F](ArtistRepository.existingArtists)
    val artistService = ArtistService.impl[F](artistRepository)
    val httpApp = (TrackMetadataApi.routes[F](trackRepository, trackService) <+> ArtistMetadataApi.routes[F](artistService)).orNotFound
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
