package net.primal.android.wallet.send.create

import net.primal.android.attachments.domain.CdnImage

interface CreateTransactionContract {

    data class UiState(
        val transaction: DraftTransaction,
        val error: Throwable? = null,
        val hasPositiveValue: Boolean = false,
        val profileAvatarCdnImage: CdnImage? = null,
        val profileDisplayName: String? = null,
        val profileLightningAddress: String? = null,
    )

    sealed class UiEvent {
        sealed class NumericInputEvent : UiEvent() {
            data class DigitInputEvent(val digit: Int) : NumericInputEvent()
            data object BackspaceEvent : NumericInputEvent()
            data object ResetAmountEvent : NumericInputEvent()
        }

        data class SendTransaction(val note: String?) : UiEvent()
    }

    sealed class SideEffect
}
