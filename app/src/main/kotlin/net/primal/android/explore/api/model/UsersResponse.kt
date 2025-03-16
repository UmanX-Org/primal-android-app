package net.primal.android.explore.api.model

import net.primal.domain.PrimalEvent
import net.primal.domain.nostr.NostrEvent

data class UsersResponse(
    val contactsMetadata: List<NostrEvent> = emptyList(),
    val userScores: PrimalEvent?,
    val followerCounts: PrimalEvent?,
    val cdnResources: List<PrimalEvent> = emptyList(),
    val primalUserNames: PrimalEvent? = null,
    val primalLegendProfiles: PrimalEvent? = null,
    val primalPremiumInfo: PrimalEvent? = null,
    val blossomServers: List<NostrEvent> = emptyList(),
)
