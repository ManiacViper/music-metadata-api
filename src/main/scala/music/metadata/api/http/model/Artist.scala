package music.metadata.api.http.model

import java.util.UUID

final case class ArtistAliasesRequestBody(aliases: Seq[String])
final case class ArtistResponse(artistId: UUID, name: String, aliases: Seq[String])
