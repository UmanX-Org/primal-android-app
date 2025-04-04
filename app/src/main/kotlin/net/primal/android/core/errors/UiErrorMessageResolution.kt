package net.primal.android.core.errors

import android.content.Context
import net.primal.android.R

@Suppress("CyclomaticComplexMethod")
fun UiError.resolveUiErrorMessage(context: Context): String {
    return when (this) {
        UiError.InvalidNaddr -> context.getString(
            R.string.app_error_invalid_naddr,
        )

        UiError.MissingPrivateKey -> context.getString(R.string.app_error_missing_private_key)

        is UiError.InvalidZapRequest -> context.getString(
            R.string.post_action_invalid_zap_request,
        )

        is UiError.MissingLightningAddress -> context.getString(
            R.string.post_action_missing_lightning_address,
        )

        is UiError.FailedToPublishZapEvent -> context.getString(
            R.string.post_action_zap_failed,
        )

        is UiError.FailedToPublishLikeEvent -> context.getString(
            R.string.post_action_like_failed,
        )

        is UiError.FailedToPublishRepostEvent -> context.getString(
            R.string.post_action_repost_failed,
        )

        is UiError.FailedToMuteUser -> context.getString(R.string.app_error_muting_user)

        is UiError.FailedToUnmuteUser -> context.getString(R.string.app_error_unmuting_user)

        is UiError.FailedToFollowUser -> context.getString(R.string.app_error_unable_to_follow_profile)

        is UiError.FailedToUnfollowUser -> context.getString(R.string.app_error_unable_to_unfollow_profile)

        is UiError.MissingRelaysConfiguration -> context.getString(
            R.string.app_missing_relays_config,
        )

        is UiError.FailedToAddToFeed -> context.getString(R.string.app_error_adding_to_feed)

        is UiError.GenericError -> context.getString(R.string.app_generic_error)

        is UiError.FailedToRemoveFeed -> context.getString(R.string.app_error_removing_feed)

        UiError.NostrSignUnauthorized -> context.getString(R.string.app_error_sign_unauthorized)
    }
}
