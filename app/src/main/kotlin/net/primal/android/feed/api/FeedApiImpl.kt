package net.primal.android.feed.api

import kotlinx.serialization.encodeToString
import net.primal.android.feed.api.model.FeedRequestBody
import net.primal.android.feed.api.model.FeedResponse
import net.primal.android.feed.api.model.ThreadRequestBody
import net.primal.android.feed.api.model.ThreadResponse
import net.primal.android.networking.sockets.SocketClient
import net.primal.android.networking.sockets.filterNostrEvents
import net.primal.android.networking.sockets.filterPrimalEvents
import net.primal.android.networking.sockets.findPrimalEvent
import net.primal.android.networking.sockets.model.OutgoingMessage
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalPaging
import net.primal.android.serialization.NostrJson
import javax.inject.Inject

class FeedApiImpl @Inject constructor(
    private val socketClient: SocketClient,
) : FeedApi {

    override suspend fun getFeed(body: FeedRequestBody): FeedResponse {
        val queryResult = socketClient.query(
            message = OutgoingMessage(
                primalVerb = "feed_directive",
                optionsJson = NostrJson.encodeToString(body)
            )
        )

        return FeedResponse(
            paging = queryResult.findPrimalEvent(NostrEventKind.PrimalPaging).pagingContentOrNull(),
            metadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            posts = queryResult.filterNostrEvents(NostrEventKind.ShortTextNote),
            reposts = queryResult.filterNostrEvents(NostrEventKind.Reposts),
            primalEventStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventStats),
            primalEventUserStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventUserStats),
            primalEventResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventResources),
            referencedPosts = queryResult.filterPrimalEvents(NostrEventKind.PrimalReferencedEvent),
        )

    }

    override suspend fun getThread(body: ThreadRequestBody): ThreadResponse {
        val queryResult = socketClient.query(
            message = OutgoingMessage(
                primalVerb = "thread_view",
                optionsJson = NostrJson.encodeToString(body)
            )
        )

        return ThreadResponse(
            metadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            posts = queryResult.filterNostrEvents(NostrEventKind.ShortTextNote),
            primalEventStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventStats),
            primalEventUserStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventUserStats),
            primalEventResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventResources),
            referencedPosts = queryResult.filterPrimalEvents(NostrEventKind.PrimalReferencedEvent),
        )
    }

    private fun PrimalEvent?.pagingContentOrNull(): ContentPrimalPaging? {
        val pagingContent = this?.content ?: return null
        return try {
            NostrJson.decodeFromString(pagingContent)
        } catch (error: IllegalArgumentException) {
            null
        }
    }
}
