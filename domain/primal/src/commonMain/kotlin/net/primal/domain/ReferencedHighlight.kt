package net.primal.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray

@Serializable
data class ReferencedHighlight(
    val text: String,
    val eventId: String?,
    val authorId: String?,
    val aTag: JsonArray,
)
