package net.primal.android.wallet.transactions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import net.primal.android.R
import net.primal.android.theme.AppTheme
import net.primal.android.user.domain.PrimalWallet
import net.primal.android.user.domain.WalletPreference
import net.primal.android.wallet.dashboard.WalletDashboardContract
import net.primal.android.wallet.dashboard.ui.WalletAction
import net.primal.android.wallet.dashboard.ui.WalletDashboard

@ExperimentalFoundationApi
@Composable
fun TransactionsLazyColumn(
    modifier: Modifier,
    walletBalance: BigDecimal?,
    primalWallet: PrimalWallet,
    walletPreference: WalletPreference,
    pagingItems: LazyPagingItems<TransactionDataUi>,
    listState: LazyListState,
    paddingValues: PaddingValues,
    onProfileClick: (String) -> Unit,
    onWalletAction: (WalletAction) -> Unit,
    eventPublisher: (WalletDashboardContract.UiEvent) -> Unit,
) {
    val today = stringResource(id = R.string.wallet_transactions_today).lowercase()
    val yesterday = stringResource(id = R.string.wallet_transactions_yesterday).lowercase()
    val numberFormat = remember { NumberFormat.getNumberInstance() }
    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = paddingValues,
    ) {
        item(
            contentType = "Balance & Actions",
        ) {
            WalletDashboard(
                modifier = Modifier
                    .wrapContentSize(align = Alignment.Center)
                    .padding(horizontal = 32.dp),
                walletBalance = walletBalance,
                primalWallet = primalWallet,
                walletPreference = walletPreference,
                actions = listOf(WalletAction.Send, WalletAction.Scan, WalletAction.Receive),
                onWalletAction = onWalletAction,
                onWalletPreferenceChanged = {
                    eventPublisher(WalletDashboardContract.UiEvent.UpdateWalletPreference(it))
                },
            )
        }

        pagingItems.itemSnapshotList.items.groupBy {
            it.txInstant.formatDay(
                todayTranslation = today,
                yesterdayTranslation = yesterday,
            )
        }.forEach { (day, dayTransactions) ->
            stickyHeader(
                key = day,
                contentType = "DayHeader",
            ) {
                TransactionsHeaderListItem(
                    modifier = Modifier
                        .background(color = AppTheme.colorScheme.surfaceVariant)
                        .fillMaxWidth(),
                    day = day,
                )
            }

            items(
                items = dayTransactions,
                key = { it.txId },
                contentType = { "Transaction" },
            ) {
                TransactionListItem(
                    data = it,
                    numberFormat = numberFormat,
                    onAvatarClick = onProfileClick,
                )
            }
        }
    }
}

private fun Instant.formatDay(todayTranslation: String, yesterdayTranslation: String): String {
    val zoneId = ZoneId.systemDefault()
    val zonedDateTime: ZonedDateTime = this.atZone(zoneId)
    val now = ZonedDateTime.now(zoneId)

    return if (now.toLocalDate() == zonedDateTime.toLocalDate()) {
        todayTranslation
    } else if (now.minusDays(1).toLocalDate() == zonedDateTime.toLocalDate()) {
        yesterdayTranslation
    } else {
        zonedDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    }
}
