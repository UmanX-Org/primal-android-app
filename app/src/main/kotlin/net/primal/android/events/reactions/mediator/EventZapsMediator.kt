package net.primal.android.events.reactions.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.db.PrimalDatabase
import net.primal.android.events.db.EventZap
import net.primal.android.events.repository.persistToDatabaseAsTransaction
import net.primal.data.remote.api.events.EventStatsApi
import net.primal.data.remote.api.events.model.EventZapsRequestBody

@ExperimentalPagingApi
class EventZapsMediator(
    private val eventId: String,
    private val userId: String,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val eventStatsApi: EventStatsApi,
    private val database: PrimalDatabase,
) : RemoteMediator<Int, EventZap>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, EventZap>): MediatorResult {
        withContext(dispatcherProvider.io()) {
            val response = eventStatsApi.getEventZaps(
                EventZapsRequestBody(
                    eventId = eventId,
                    userId = userId,
                    limit = 100,
                ),
            )
            response.persistToDatabaseAsTransaction(database)
        }
        return MediatorResult.Success(endOfPaginationReached = true)
    }
}
