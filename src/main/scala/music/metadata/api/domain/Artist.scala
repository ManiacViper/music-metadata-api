package music.metadata.api.domain

import java.util.UUID

case class Artist(id: UUID, name: String, aliases: List[String])
