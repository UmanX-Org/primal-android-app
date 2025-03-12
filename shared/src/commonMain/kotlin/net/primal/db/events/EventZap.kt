package net.primal.db.events

import androidx.room.Entity
import net.primal.db.profiles.PrimalLegendProfile
import net.primal.domain.CdnImage

@Entity(
    primaryKeys = [
        "zapSenderId",
        "eventId",
        "zapRequestAt",
    ],
)
data class EventZap(
    val eventId: String,
    val zapSenderId: String,
    val zapReceiverId: String,
    val zapRequestAt: Long,
    val zapReceiptAt: Long,
    val amountInBtc: Double,
    val message: String?,
    val zapSenderDisplayName: String? = null,
    val zapSenderHandle: String? = null,
    val zapSenderInternetIdentifier: String? = null,
    val zapSenderAvatarCdnImage: CdnImage? = null,
    val zapSenderPrimalLegendProfile: PrimalLegendProfile? = null,
)
