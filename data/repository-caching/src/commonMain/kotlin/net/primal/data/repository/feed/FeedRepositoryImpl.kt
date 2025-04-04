package net.primal.data.repository.feed

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.primal.core.networking.sockets.errors.WssException
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.dao.notes.FeedPost as FeedPostPO
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.local.queries.ChronologicalFeedWithRepostsQueryBuilder
import net.primal.data.local.queries.ExploreFeedQueryBuilder
import net.primal.data.local.queries.FeedQueryBuilder
import net.primal.data.remote.api.feed.FeedApi
import net.primal.data.remote.api.feed.model.ThreadRequestBody
import net.primal.data.repository.feed.paging.NoteFeedRemoteMediator
import net.primal.data.repository.feed.processors.persistNoteRepliesAndArticleCommentsToDatabase
import net.primal.data.repository.feed.processors.persistToDatabaseAsTransaction
import net.primal.data.repository.mappers.local.mapAsFeedPostDO
import net.primal.domain.error.NetworkException
import net.primal.domain.model.FeedPost as FeedPostDO
import net.primal.domain.repository.FeedRepository
import net.primal.domain.supportsNoteReposts

internal class FeedRepositoryImpl(
    private val feedApi: FeedApi,
    private val database: PrimalDatabase,
    private val dispatcherProvider: DispatcherProvider,
) : FeedRepository {

    override fun feedBySpec(userId: String, feedSpec: String): Flow<PagingData<FeedPostDO>> {
        return createPager(userId = userId, feedSpec = feedSpec) {
            database.feedPosts().feedQuery(
                query = feedQueryBuilder(userId = userId, feedSpec = feedSpec).feedQuery(),
            )
        }.flow.map { it.map { feedPostPO -> feedPostPO.mapAsFeedPostDO() } }
    }

    override suspend fun findAllPostsByIds(postIds: List<String>): List<FeedPostDO> =
        withContext(dispatcherProvider.io()) {
            database.feedPosts().findAllPostsByIds(postIds).map { it.mapAsFeedPostDO() }
        }

    override suspend fun fetchConversation(userId: String, noteId: String) {
        withContext(dispatcherProvider.io()) {
            val response = try {
                feedApi.getThread(ThreadRequestBody(postId = noteId, userPubKey = userId, limit = 100))
            } catch (error: WssException) {
                throw NetworkException(message = error.message, cause = error)
            }
            response.persistNoteRepliesAndArticleCommentsToDatabase(noteId = noteId, database = database)
            response.persistToDatabaseAsTransaction(userId = userId, database = database)
        }
    }

    override suspend fun findConversation(userId: String, noteId: String): List<FeedPostDO> {
        return observeConversation(userId = userId, noteId = noteId).firstOrNull() ?: emptyList()
    }

    override fun observeConversation(userId: String, noteId: String): Flow<List<FeedPostDO>> {
        return database.threadConversations().observeNoteConversation(
            postId = noteId,
            userId = userId,
        ).map { list -> list.map { it.mapAsFeedPostDO() } }
    }

    @OptIn(ExperimentalPagingApi::class)
    private fun createPager(
        userId: String,
        feedSpec: String,
        pagingSourceFactory: () -> PagingSource<Int, FeedPostPO>,
    ) = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            prefetchDistance = PAGE_SIZE,
            initialLoadSize = PAGE_SIZE * 3,
            enablePlaceholders = true,
        ),
        remoteMediator = NoteFeedRemoteMediator(
            dispatcherProvider = dispatcherProvider,
            feedSpec = feedSpec,
            userId = userId,
            feedApi = feedApi,
            database = database,
        ),
        pagingSourceFactory = pagingSourceFactory,
    )

    private fun feedQueryBuilder(userId: String, feedSpec: String): FeedQueryBuilder =
        when {
            feedSpec.supportsNoteReposts() -> ChronologicalFeedWithRepostsQueryBuilder(
                feedSpec = feedSpec,
                userPubkey = userId,
            )

            else -> ExploreFeedQueryBuilder(
                feedSpec = feedSpec,
                userPubkey = userId,
            )
        }

    companion object {
        private const val PAGE_SIZE = 25
    }
}
