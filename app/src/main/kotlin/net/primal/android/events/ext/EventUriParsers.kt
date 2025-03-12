package net.primal.android.events.ext

import net.primal.android.articles.db.ArticleData
import net.primal.android.core.utils.detectMimeType
import net.primal.android.events.db.EventUri
import net.primal.android.events.domain.CdnResource
import net.primal.android.events.domain.EventLinkPreviewData
import net.primal.android.events.domain.EventUriType
import net.primal.android.messages.db.DirectMessageData
import net.primal.android.nostr.ext.isNostrUri
import net.primal.android.notes.db.PostData

private data class EventLink(
    val eventId: String,
    val uri: String,
)

fun List<PostData>.flatMapPostsAsEventUriPO(
    cdnResources: Map<String, CdnResource>,
    linkPreviews: Map<String, EventLinkPreviewData>,
    videoThumbnails: Map<String, String>,
) = flatMap { postData ->
    postData.uris.map { uri -> EventLink(eventId = postData.postId, uri = uri) }
}
    .filterNot { it.uri.isNostrUri() }
    .mapToEventUri(
        cdnResources = cdnResources,
        linkPreviews = linkPreviews,
        videoThumbnails = videoThumbnails,
    )

fun List<DirectMessageData>.flatMapMessagesAsEventUriPO() =
    flatMap { messageData ->
        messageData.uris.map { uri -> EventLink(eventId = messageData.messageId, uri = uri) }
    }
        .filterNot { it.uri.isNostrUri() }
        .map { (eventId, uri) ->
            val mimeType = uri.detectMimeType()
            EventUri(
                eventId = eventId,
                url = uri,
                type = detectEventUriType(url = uri, mimeType = mimeType),
                mimeType = mimeType,
            )
        }

fun List<ArticleData>.flatMapArticlesAsEventUriPO(
    cdnResources: Map<String, CdnResource>,
    linkPreviews: Map<String, EventLinkPreviewData>,
    videoThumbnails: Map<String, String>,
) = flatMap { articleData ->
    val uriAttachments = articleData.uris.map { uri ->
        EventLink(eventId = articleData.eventId, uri = uri)
    }

    val imageAttachment = articleData.imageCdnImage?.sourceUrl?.let { imageUrl ->
        listOf(EventLink(eventId = articleData.eventId, uri = imageUrl))
    } ?: emptyList()

    imageAttachment + uriAttachments
}
    .filterNot { it.uri.isNostrUri() }
    .mapToEventUri(
        cdnResources = cdnResources,
        linkPreviews = linkPreviews,
        videoThumbnails = videoThumbnails,
    )

private fun List<EventLink>.mapToEventUri(
    cdnResources: Map<String, CdnResource>,
    linkPreviews: Map<String, EventLinkPreviewData>,
    videoThumbnails: Map<String, String>,
): List<EventUri> =
    map { (eventId, uri) ->
        val uriCdnResource = cdnResources[uri]
        val linkPreview = linkPreviews[uri]
        val linkThumbnailCdnResource = linkPreview?.thumbnailUrl?.let { cdnResources[it] }
        val videoThumbnail = videoThumbnails[uri]
        val mimeType = uri.detectMimeType() ?: uriCdnResource?.contentType ?: linkPreview?.mimeType
        val type = detectEventUriType(url = uri, mimeType = mimeType)

        EventUri(
            eventId = eventId,
            url = uri,
            type = type,
            mimeType = mimeType,
            variants = (uriCdnResource?.variants ?: emptyList()) + (linkThumbnailCdnResource?.variants ?: emptyList()),
            title = linkPreview?.title?.ifBlank { null },
            description = linkPreview?.description?.ifBlank { null },
            thumbnail = linkPreview?.thumbnailUrl?.ifBlank { null } ?: videoThumbnail,
            authorAvatarUrl = linkPreview?.authorAvatarUrl?.ifBlank { null },
        )
    }

private fun detectEventUriType(url: String, mimeType: String?): EventUriType {
    mimeType?.let {
        val eventUriType = detectEventUriTypeByMimeType(mimeType)
        if (eventUriType != EventUriType.Other) {
            return eventUriType
        }
    }

    return detectEventUriTypeByUrl(url)
}

private fun detectEventUriTypeByMimeType(mimeType: String): EventUriType {
    return when {
        mimeType.startsWith("image") -> EventUriType.Image
        mimeType.startsWith("video") -> EventUriType.Video
        mimeType.startsWith("audio") -> EventUriType.Audio
        mimeType.endsWith("pdf") -> EventUriType.Pdf
        else -> EventUriType.Other
    }
}

private fun detectEventUriTypeByUrl(url: String): EventUriType {
    return when {
        url.contains(".youtube.com") -> EventUriType.YouTube
        url.contains("/youtube.com") -> EventUriType.YouTube
        url.contains("/youtu.be") -> EventUriType.YouTube
        url.contains(".rumble.com") || url.contains("/rumble.com") -> EventUriType.Rumble
        url.contains("/open.spotify.com/") -> EventUriType.Spotify
        url.contains("/listen.tidal.com/") -> EventUriType.Tidal
        url.contains("/github.com/") -> EventUriType.GitHub
        else -> EventUriType.Other
    }
}
