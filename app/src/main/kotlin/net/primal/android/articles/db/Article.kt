package net.primal.android.articles.db

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.android.bookmarks.db.PublicBookmark
import net.primal.android.events.db.EventStats
import net.primal.android.events.db.EventUserStats
import net.primal.android.events.db.EventZap
import net.primal.android.highlights.db.Highlight
import net.primal.android.highlights.db.HighlightData
import net.primal.android.profile.db.ProfileData

data class Article(
    @Embedded
    val data: ArticleData,

    @Relation(entityColumn = "ownerId", parentColumn = "authorId")
    val author: ProfileData? = null,

    @Relation(entityColumn = "eventId", parentColumn = "eventId")
    val eventStats: EventStats? = null,

    @Relation(entityColumn = "eventId", parentColumn = "eventId")
    val userEventStats: EventUserStats? = null,

    @Relation(entityColumn = "eventId", parentColumn = "eventId")
    val eventZaps: List<EventZap> = emptyList(),

    @Relation(entityColumn = "tagValue", parentColumn = "aTag")
    val bookmark: PublicBookmark? = null,

    @Relation(entity = HighlightData::class, entityColumn = "referencedEventATag", parentColumn = "aTag")
    val highlights: List<Highlight> = emptyList(),
)
