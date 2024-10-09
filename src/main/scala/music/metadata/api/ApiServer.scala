package music.metadata.api

import cats.effect.Async
import com.comcast.ip4s._
import fs2.io.net.Network
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

object ApiServer {

  def run[F[_]: Async: Network]: F[Nothing] = {
    val trackService = TrackRepository.impl[F]
    val httpApp = (TrackMetadataApi.routes[F](trackService)).orNotFound
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
