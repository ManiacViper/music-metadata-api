package music.metadata.api.http.model
import music.metadata.api.domain.Genre

import java.util.UUID

final case class TrackResponse(id: UUID, title: String, genre: Genre, lengthInSeconds: Long)
final case class TracksResponse(artistId: UUID, tracks: List[TrackResponse])
