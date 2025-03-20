package net.primal.data.remote.factory

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import net.primal.core.networking.factory.HttpClientFactory
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.data.remote.api.articles.ArticlesApi
import net.primal.data.remote.api.articles.ArticlesApiImpl
import net.primal.data.remote.api.events.EventStatsApi
import net.primal.data.remote.api.events.EventStatsApiImpl
import net.primal.data.remote.api.explore.ExploreApi
import net.primal.data.remote.api.explore.ExploreApiImpl
import net.primal.data.remote.api.feed.FeedApi
import net.primal.data.remote.api.feed.FeedApiImpl
import net.primal.data.remote.api.importing.PrimalImportApi
import net.primal.data.remote.api.importing.PrimalImportApiImpl
import net.primal.data.remote.api.users.UserWellKnownApi
import net.primal.data.remote.api.users.UsersApi
import net.primal.data.remote.api.users.UsersApiImpl

object PrimalApiFactory {

    private val defaultHttpClient = HttpClientFactory.createHttpClientWithDefaultConfig()

    fun createArticlesApi(primalApiClient: PrimalApiClient): ArticlesApi = ArticlesApiImpl(primalApiClient)

    fun createEventsApi(primalApiClient: PrimalApiClient): EventStatsApi = EventStatsApiImpl(primalApiClient)

    fun createExploreApi(primalApiClient: PrimalApiClient): ExploreApi = ExploreApiImpl(primalApiClient)

    fun createFeedsApi(primalApiClient: PrimalApiClient): FeedApi = FeedApiImpl(primalApiClient)

    fun createImportApi(primalApiClient: PrimalApiClient): PrimalImportApi = PrimalImportApiImpl(primalApiClient)

    fun createUsersApi(primalApiClient: PrimalApiClient): UsersApi = UsersApiImpl(primalApiClient)

    fun createUserWellKnownApi(httpClient: HttpClient = defaultHttpClient): UserWellKnownApi =
        Ktorfit.Builder()
            .baseUrl("https://primal.net/")
            .httpClient(client = httpClient)
            .build()
            .create()
}
