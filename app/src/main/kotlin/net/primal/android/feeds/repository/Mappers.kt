package net.primal.android.feeds.repository

import net.primal.android.feeds.db.Feed
import net.primal.android.nostr.model.primal.content.ContentArticleFeedData
import net.primal.domain.FeedSpecKind

fun ContentArticleFeedData.asFeedPO(ownerId: String, specKind: FeedSpecKind): Feed {
    return Feed(
        ownerId = ownerId,
        spec = this.spec,
        specKind = specKind,
        title = this.name,
        description = this.description,
        enabled = this.enabled,
        feedKind = this.feedKind,
    )
}
