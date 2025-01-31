package net.primal.android.profile.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrimalLegendProfile(
    @SerialName("style") val styleId: String,
    @SerialName("custom_badge") val customBadge: Boolean,
    @SerialName("avatar_glow") val avatarGlow: Boolean,
    @SerialName("legend_since") val legendSince: Long? = null,
    @SerialName("in_leaderboard") val inLeaderboard: Boolean? = null,
    @SerialName("current_shoutout") val currentShoutout: String? = null,
)
