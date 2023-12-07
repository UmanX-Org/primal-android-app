package net.primal.android.wallet.ext

import net.primal.android.core.compose.feed.model.ZappingState
import net.primal.android.user.domain.UserAccount
import net.primal.android.user.domain.WalletPreference
import net.primal.android.wallet.domain.WalletKycLevel
import net.primal.android.wallet.utils.CurrencyConversionUtils.toSats

fun UserAccount.hasWallet(): Boolean {
    return when (walletPreference) {
        WalletPreference.NostrWalletConnect -> nostrWallet != null
        else -> primalWallet != null && primalWallet.kycLevel != WalletKycLevel.None
    }
}

fun ZappingState.canZap(zapAmount: ULong = this.defaultZapAmount): Boolean {
    return walletConnected && when (walletPreference) {
        WalletPreference.NostrWalletConnect -> true
        else -> (walletBalanceInBtc == null || walletBalanceInBtc.toSats() >= zapAmount)
    }
}
