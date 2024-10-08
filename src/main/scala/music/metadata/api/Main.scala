package music.metadata.api

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple {
  val run = MusicmetadataapiServer.run[IO]
}
