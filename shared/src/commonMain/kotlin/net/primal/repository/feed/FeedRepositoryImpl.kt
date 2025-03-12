package net.primal.repository.feed

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import net.primal.api.feed.FeedApi
import net.primal.api.feed.model.ThreadRequestBody
import net.primal.core.coroutines.DispatcherProvider
import net.primal.db.PrimalDatabase
import net.primal.db.notes.FeedPost
import net.primal.repository.processors.persistNoteRepliesAndArticleCommentsToDatabase
import net.primal.repository.processors.persistToDatabaseAsTransaction

internal class FeedRepositoryImpl(
    private val feedApi: FeedApi,
    private val database: PrimalDatabase,
    private val dispatcherProvider: DispatcherProvider,
) : FeedRepository {

    override suspend fun findAllPostsByIds(postIds: List<String>): List<FeedPost> =
        withContext(dispatcherProvider.io()) {
            database.feedPosts().findAllPostsByIds(postIds)
        }

    override suspend fun fetchConversation(userId: String, noteId: String) {
        withContext(dispatcherProvider.io()) {
            val response = feedApi.getThread(ThreadRequestBody(postId = noteId, userPubKey = userId, limit = 100))
            response.persistNoteRepliesAndArticleCommentsToDatabase(noteId = noteId, database = database)
            response.persistToDatabaseAsTransaction(userId = userId, database = database)
        }
    }

    override suspend fun findConversation(userId: String, noteId: String): List<FeedPost> {
        return observeConversation(userId = userId, noteId = noteId).firstOrNull() ?: emptyList()
    }

    override fun observeConversation(userId: String, noteId: String): Flow<List<FeedPost>> {
        return database.threadConversations().observeNoteConversation(
            postId = noteId,
            userId = userId,
        )
    }

}
