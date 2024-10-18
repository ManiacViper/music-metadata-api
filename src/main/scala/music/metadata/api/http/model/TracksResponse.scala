package music.metadata.api.http.model
import music.metadata.api.domain.Track

import java.util.UUID

final case class TrackResponse(id: UUID, title: String, genre: String, lengthInSeconds: Long)
object TracksResponse {
  def from(artistId: UUID, tracks: Seq[Track]): TracksResponse = {
    TracksResponse(
      artistId,
      tracks.map(track => TrackResponse(track.id, track.title, track.genre.toString, track.lengthInSeconds))
    )
  }
}
final case class TracksResponse(artistId: UUID, tracks: Seq[TrackResponse])
