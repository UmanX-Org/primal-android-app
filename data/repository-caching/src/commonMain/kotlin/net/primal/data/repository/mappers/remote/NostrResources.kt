package net.primal.data.repository.mappers.remote

import io.github.aakira.napier.Napier
import kotlinx.serialization.json.JsonArray
import net.primal.data.local.dao.events.EventUriNostr
import net.primal.data.local.dao.messages.DirectMessageData
import net.primal.data.local.dao.notes.PostData
import net.primal.data.local.dao.profiles.ProfileData
import net.primal.data.local.dao.reads.ArticleData
import net.primal.data.repository.mappers.authorNameUiFriendly
import net.primal.data.repository.mappers.usernameUiFriendly
import net.primal.domain.CdnResource
import net.primal.domain.EventLinkPreviewData
import net.primal.domain.EventUriNostrReference
import net.primal.domain.EventUriNostrType
import net.primal.domain.ReferencedArticle
import net.primal.domain.ReferencedHighlight
import net.primal.domain.ReferencedNote
import net.primal.domain.ReferencedUser
import net.primal.domain.ReferencedZap
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nevent
import net.primal.domain.nostr.Nip19TLV
import net.primal.domain.nostr.Nip19TLV.readAsString
import net.primal.domain.nostr.Nip19TLV.toNaddrString
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.cryptography.bech32ToHexOrThrow
import net.primal.domain.nostr.cryptography.bechToBytesOrThrow
import net.primal.domain.nostr.cryptography.toHex
import net.primal.domain.nostr.findFirstAltDescription
import net.primal.domain.nostr.findFirstEventId
import net.primal.domain.nostr.findFirstProfileId
import net.primal.domain.nostr.isATag
import net.primal.domain.nostr.utils.asEllipsizedNpub
import net.primal.domain.utils.wordsCountToReadingTime

private const val NOSTR = "nostr:"
private const val NPUB = "npub1"
private const val NSEC = "nsec1"
private const val NEVENT = "nevent1"
private const val NADDR = "naddr1"
private const val NOTE = "note1"
private const val NRELAY = "nrelay1"
private const val NPROFILE = "nprofile1"

private val nostrUriRegexPattern: Regex = Regex(
    "($NOSTR)?@?($NSEC|$NPUB|$NEVENT|$NADDR|$NOTE|$NPROFILE|$NRELAY)([qpzry9x8gf2tvdw0s3jn54khce6mua7l]+)(\\S*)",
    RegexOption.IGNORE_CASE,
)

fun String.isNostrUri(): Boolean {
    val uri = lowercase()
    return uri.startsWith(NOSTR) || uri.startsWith(NPUB) || uri.startsWith(NOTE) ||
        uri.startsWith(NEVENT) || uri.startsWith(NPROFILE)
}

fun String.cleanNostrUris(): String =
    this
        .replace("@$NOSTR", NOSTR)
        .replace("@$NPUB", NPUB)
        .replace("@$NOTE", NOTE)
        .replace("@$NEVENT", NEVENT)
        .replace("@$NADDR", NADDR)
        .replace("@$NPROFILE", NPROFILE)

fun String.isNote() = lowercase().startsWith(NOTE)

fun String.isNPub() = lowercase().startsWith(NPUB)

fun String.isNProfile() = lowercase().startsWith(NPROFILE)

fun String.isNAddr() = lowercase().startsWith(NADDR)

fun String.isNEvent() = lowercase().startsWith(NEVENT)

fun String.isNoteUri() = lowercase().startsWith(NOSTR + NOTE)

fun String.isNEventUri() = lowercase().startsWith(NOSTR + NEVENT)

fun String.isNPubUri() = lowercase().startsWith(NOSTR + NPUB)

fun String.isNProfileUri() = lowercase().startsWith(NOSTR + NPROFILE)

fun String.isNAddrUri() = lowercase().startsWith(NOSTR + NADDR)

private fun String.nostrUriToBytes(): ByteArray? {
    val matchResult = nostrUriRegexPattern.find(this) ?: return null
    val type = matchResult.groupValues[2].takeIf { it.isNotEmpty() }?.lowercase() ?: return null
    val key = matchResult.groupValues[3].takeIf { it.isNotEmpty() }?.lowercase() ?: return null
    return try {
        (type + key).bechToBytesOrThrow()
    } catch (ignored: Exception) {
        Napier.w("", ignored)
        null
    }
}

fun String.nostrUriToNoteId() = nostrUriToBytes()?.toHex()

fun String.nostrUriToPubkey() = nostrUriToBytes()?.toHex()

private fun String.nostrUriToIdAndRelay(): Pair<String?, String?> {
    val bytes = nostrUriToBytes() ?: return null to null
    val tlv = Nip19TLV.parse(bytes)
    val id = tlv[Nip19TLV.Type.SPECIAL.id]?.firstOrNull()?.toHex()
    val relayBytes = tlv[Nip19TLV.Type.RELAY.id]?.firstOrNull()
    return id to relayBytes?.readAsString()
}

fun String.nostrUriToNoteIdAndRelay() = nostrUriToIdAndRelay()

fun String.nostrUriToPubkeyAndRelay() = nostrUriToIdAndRelay()

fun String.extractProfileId(): String? {
    return extract { bechPrefix: String?, key: String? ->
        when (bechPrefix?.lowercase()) {
            NPUB -> (bechPrefix + key).bechToBytesOrThrow().toHex()
            NPROFILE -> {
                val tlv = Nip19TLV.parse((bechPrefix + key).bechToBytesOrThrow())
                tlv[Nip19TLV.Type.SPECIAL.id]?.first()?.toHex()
            }

            else -> null
        }
    }
}

fun String.extractNoteId(): String? {
    return extract { bechPrefix: String?, key: String? ->
        when (bechPrefix?.lowercase()) {
            NOTE -> (bechPrefix + key).bechToBytesOrThrow().toHex()
            NEVENT -> {
                val tlv = Nip19TLV.parse((bechPrefix + key).bechToBytesOrThrow())
                tlv[Nip19TLV.Type.SPECIAL.id]?.first()?.toHex()
            }

            else -> null
        }
    }
}

fun String.extractEventId(): String? {
    return extract { bechPrefix: String?, key: String? ->
        when (bechPrefix?.lowercase()) {
            NEVENT -> {
                val tlv = Nip19TLV.parse((bechPrefix + key).bechToBytesOrThrow())
                tlv[Nip19TLV.Type.SPECIAL.id]?.first()?.toHex()
            }

            else -> (bechPrefix + key).bechToBytesOrThrow().toHex()
        }
    }
}

fun PostData.toNevent() =
    Nevent(
        userId = this.authorId,
        eventId = this.postId,
        kind = NostrEventKind.ShortTextNote.value,
        relays = emptyList(),
    )

private fun String.extract(parser: (bechPrefix: String?, key: String?) -> String?): String? {
    val matchResult = nostrUriRegexPattern.find(this) ?: return null

    val bechPrefix = matchResult.groupValues.getOrNull(2)
    val key = matchResult.groupValues.getOrNull(3)

    return try {
        parser(bechPrefix, key)
    } catch (ignored: Exception) {
        Napier.w("", ignored)
        null
    }
}

fun String.takeAsNaddrOrNull(): String? {
    return if (isNAddr() || isNAddrUri()) {
        val result = runCatching {
            Nip19TLV.parseUriAsNaddrOrNull(this)
        }
        if (result.getOrNull() != null) {
            this
        } else {
            null
        }
    } else {
        null
    }
}

fun String.takeAsNoteHexIdOrNull(): String? {
    return if (isNote() || isNoteUri() || isNEventUri() || isNEvent()) {
        val result = runCatching { this.extractNoteId() }
        result.getOrNull()
    } else {
        null
    }
}

fun String.takeAsProfileHexIdOrNull(): String? {
    return if (isNPub() || isNPubUri()) {
        val result = runCatching {
            this.bech32ToHexOrThrow()
        }
        result.getOrNull()
    } else {
        null
    }
}

fun List<EventUriNostrReference>.mapReferencedNostrUriAsEventUriNostrPO() =
    map {
        EventUriNostr(
            eventId = it.eventId,
            uri = it.uri,
            type = it.type,
            referencedEventAlt = it.referencedEventAlt,
            referencedHighlight = it.referencedHighlight,
            referencedNote = it.referencedNote,
            referencedArticle = it.referencedArticle,
            referencedUser = it.referencedUser,
            referencedZap = it.referencedZap,
        )
    }

fun List<PostData>.flatMapPostsAsReferencedNostrUriDO(
    eventIdToNostrEvent: Map<String, NostrEvent>,
    postIdToPostDataMap: Map<String, PostData>,
    articleIdToArticle: Map<String, ArticleData>,
    profileIdToProfileDataMap: Map<String, ProfileData>,
    cdnResources: Map<String, CdnResource>,
    linkPreviews: Map<String, EventLinkPreviewData>,
    videoThumbnails: Map<String, String>,
): List<EventUriNostrReference> =
    flatMap { postData ->
        postData.uris.mapAsReferencedNostrUriDO(
            eventId = postData.postId,
            eventIdToNostrEvent = eventIdToNostrEvent,
            postIdToPostDataMap = postIdToPostDataMap,
            articleIdToArticle = articleIdToArticle,
            profileIdToProfileDataMap = profileIdToProfileDataMap,
            cdnResources = cdnResources,
            linkPreviews = linkPreviews,
            videoThumbnails = videoThumbnails,
        )
    }

fun List<DirectMessageData>.flatMapMessagesAsReferencedNostrUriDO(
    eventIdToNostrEvent: Map<String, NostrEvent>,
    postIdToPostDataMap: Map<String, PostData>,
    articleIdToArticle: Map<String, ArticleData>,
    profileIdToProfileDataMap: Map<String, ProfileData>,
    cdnResources: Map<String, CdnResource>,
    linkPreviews: Map<String, EventLinkPreviewData>,
    videoThumbnails: Map<String, String>,
) = flatMap { messageData ->
    messageData.uris.mapAsReferencedNostrUriDO(
        eventId = messageData.messageId,
        eventIdToNostrEvent = eventIdToNostrEvent,
        postIdToPostDataMap = postIdToPostDataMap,
        articleIdToArticle = articleIdToArticle,
        profileIdToProfileDataMap = profileIdToProfileDataMap,
        cdnResources = cdnResources,
        linkPreviews = linkPreviews,
        videoThumbnails = videoThumbnails,
    )
}

fun List<String>.mapAsReferencedNostrUriDO(
    eventId: String,
    eventIdToNostrEvent: Map<String, NostrEvent>,
    postIdToPostDataMap: Map<String, PostData>,
    articleIdToArticle: Map<String, ArticleData>,
    profileIdToProfileDataMap: Map<String, ProfileData>,
    cdnResources: Map<String, CdnResource>,
    linkPreviews: Map<String, EventLinkPreviewData>,
    videoThumbnails: Map<String, String>,
) = filter { it.isNostrUri() }.map { link ->
    val refUserProfileId = link.extractProfileId()

    val refNoteId = link.extractNoteId()
    val refNote = postIdToPostDataMap[refNoteId]
    val refPostAuthor = profileIdToProfileDataMap[refNote?.authorId]

    // Rewire once Nip19 is implemented for KMP
    val refNaddr: Naddr? = null // Nip19TLV.parseUriAsNaddrOrNull(link)
    val refArticle = articleIdToArticle[refNaddr?.identifier]
    val refArticleAuthor = profileIdToProfileDataMap[refNaddr?.userId]

    val referencedNostrEvent: NostrEvent? = eventIdToNostrEvent[link.extractEventId()]

    val refHighlightText = referencedNostrEvent?.content
    val refHighlightATag = referencedNostrEvent?.tags?.firstOrNull { it.isATag() }

    val type = when {
        refUserProfileId != null -> EventUriNostrType.Profile
        refNote != null && refPostAuthor != null -> EventUriNostrType.Note
        refNaddr?.kind == NostrEventKind.LongFormContent.value &&
            refArticle != null && refArticleAuthor != null -> EventUriNostrType.Article

        referencedNostrEvent?.kind == NostrEventKind.Highlight.value &&
            refHighlightText?.isNotEmpty() == true && refHighlightATag != null -> EventUriNostrType.Highlight

        referencedNostrEvent?.kind == NostrEventKind.Zap.value -> EventUriNostrType.Zap

        else -> EventUriNostrType.Unsupported
    }

    EventUriNostrReference(
        eventId = eventId,
        uri = link,
        type = type,
        referencedEventAlt = referencedNostrEvent?.tags?.findFirstAltDescription(),
        referencedUser = takeAsReferencedUserOrNull(refUserProfileId, profileIdToProfileDataMap),
        referencedNote = takeAsReferencedNoteOrNull(
            refNote = refNote,
            refPostAuthor = refPostAuthor,
            cdnResources = cdnResources,
            linkPreviews = linkPreviews,
            videoThumbnails = videoThumbnails,
            eventIdToNostrEvent = eventIdToNostrEvent,
            postIdToPostDataMap = postIdToPostDataMap,
            articleIdToArticle = articleIdToArticle,
            profileIdToProfileDataMap = profileIdToProfileDataMap,
        ),
        referencedArticle = takeAsReferencedArticleOrNull(refNaddr, refArticle, refArticleAuthor),
        referencedZap = takeAsReferencedZapOrNull(
            event = referencedNostrEvent,
            profilesMap = profileIdToProfileDataMap,
            postsMap = postIdToPostDataMap,
            cdnResourcesMap = cdnResources,
            linkPreviewsMap = linkPreviews,
            nostrEventsMap = eventIdToNostrEvent,
            videoThumbnailsMap = videoThumbnails,
            articlesMap = articleIdToArticle,
        ),
        referencedHighlight = takeAsReferencedHighlightOrNull(
            uri = link,
            highlight = refHighlightText,
            aTag = refHighlightATag,
            authorId = referencedNostrEvent?.tags?.findFirstProfileId(),
        ),
    )
}

private fun takeAsReferencedNoteOrNull(
    refNote: PostData?,
    refPostAuthor: ProfileData?,
    cdnResources: Map<String, CdnResource>,
    linkPreviews: Map<String, EventLinkPreviewData>,
    videoThumbnails: Map<String, String>,
    eventIdToNostrEvent: Map<String, NostrEvent>,
    postIdToPostDataMap: Map<String, PostData>,
    articleIdToArticle: Map<String, ArticleData>,
    profileIdToProfileDataMap: Map<String, ProfileData>,
) = if (refNote != null && refPostAuthor != null) {
    ReferencedNote(
        postId = refNote.postId,
        createdAt = refNote.createdAt,
        content = refNote.content,
        authorId = refNote.authorId,
        authorName = refPostAuthor.authorNameUiFriendly(),
        authorAvatarCdnImage = refPostAuthor.avatarCdnImage,
        authorInternetIdentifier = refPostAuthor.internetIdentifier,
        authorLightningAddress = refPostAuthor.lightningAddress,
        authorLegendProfile = refPostAuthor.primalPremiumInfo?.legendProfile,
        attachments = listOf(refNote).flatMapPostsAsEventUriPO(
            cdnResources = cdnResources,
            linkPreviews = linkPreviews,
            videoThumbnails = videoThumbnails,
        ).mapEventUriAsNoteLinkDO(),
        nostrUris = listOf(refNote).flatMapPostsAsReferencedNostrUriDO(
            eventIdToNostrEvent = eventIdToNostrEvent,
            postIdToPostDataMap = postIdToPostDataMap,
            articleIdToArticle = articleIdToArticle,
            profileIdToProfileDataMap = profileIdToProfileDataMap,
            cdnResources = cdnResources,
            linkPreviews = linkPreviews,
            videoThumbnails = videoThumbnails,
        ),
        raw = refNote.raw,
    )
} else {
    null
}

private fun takeAsReferencedUserOrNull(
    refUserProfileId: String?,
    profileIdToProfileDataMap: Map<String, ProfileData>,
) = if (refUserProfileId != null) {
    ReferencedUser(
        userId = refUserProfileId,
        handle = profileIdToProfileDataMap[refUserProfileId]?.usernameUiFriendly()
            ?: refUserProfileId.asEllipsizedNpub(),
    )
} else {
    null
}

private fun takeAsReferencedArticleOrNull(
    refNaddr: Naddr?,
    refArticle: ArticleData?,
    refArticleAuthor: ProfileData?,
) = if (
    refNaddr?.kind == NostrEventKind.LongFormContent.value &&
    refArticle != null &&
    refArticleAuthor != null
) {
    ReferencedArticle(
        naddr = refNaddr.toNaddrString(),
        aTag = refArticle.aTag,
        eventId = refArticle.eventId,
        articleId = refArticle.articleId,
        articleTitle = refArticle.title,
        authorId = refArticle.authorId,
        authorName = refArticleAuthor.authorNameUiFriendly(),
        authorAvatarCdnImage = refArticleAuthor.avatarCdnImage,
        authorLegendProfile = refArticleAuthor.primalPremiumInfo?.legendProfile,
        createdAt = refArticle.createdAt,
        raw = refArticle.raw,
        articleImageCdnImage = refArticle.imageCdnImage,
        articleReadingTimeInMinutes = refArticle.wordsCount.wordsCountToReadingTime(),
    )
} else {
    null
}

private fun takeAsReferencedHighlightOrNull(
    uri: String,
    highlight: String?,
    aTag: JsonArray?,
    authorId: String?,
) = if (highlight?.isNotEmpty() == true && aTag != null) {
    val nevent = Nip19TLV.parseUriAsNeventOrNull(neventUri = uri)
    ReferencedHighlight(
        text = highlight,
        aTag = aTag,
        eventId = nevent?.eventId,
        authorId = authorId,
    )
} else {
    null
}

private fun takeAsReferencedZapOrNull(
    event: NostrEvent?,
    profilesMap: Map<String, ProfileData>,
    postsMap: Map<String, PostData>,
    cdnResourcesMap: Map<String, CdnResource>,
    linkPreviewsMap: Map<String, EventLinkPreviewData>,
    nostrEventsMap: Map<String, NostrEvent>,
    videoThumbnailsMap: Map<String, String>,
    articlesMap: Map<String, ArticleData>,
): ReferencedZap? {
    val zapRequest = event?.extractZapRequestOrNull()

    val receiverId = event?.tags?.findFirstProfileId()

    val senderId = zapRequest?.pubKey

    val noteId = event?.tags?.findFirstEventId()
        ?: zapRequest?.tags?.findFirstEventId()

    // TODO Rewire once LN utils are implemented in KMP
    val amountInSats = 888888
//    val amountInSats = (event?.tags?.findFirstBolt11() ?: zapRequest?.tags?.findFirstZapAmount())
//        ?.let(LnInvoiceUtils::getAmountInSats)

    if (receiverId == null || senderId == null || amountInSats == null) return null

    val zappedPost = postsMap[noteId]

    val nostrUris = listOfNotNull(zappedPost).flatMapPostsAsReferencedNostrUriDO(
        eventIdToNostrEvent = nostrEventsMap,
        postIdToPostDataMap = postsMap,
        articleIdToArticle = articlesMap,
        profileIdToProfileDataMap = profilesMap,
        cdnResources = cdnResourcesMap,
        videoThumbnails = videoThumbnailsMap,
        linkPreviews = linkPreviewsMap,
    )

    val sender = profilesMap[senderId]
    val receiver = profilesMap[receiverId]
    return ReferencedZap(
        senderId = senderId,
        senderAvatarCdnImage = sender?.avatarCdnImage,
        senderPrimalLegendProfile = sender?.primalPremiumInfo?.legendProfile,
        receiverId = receiverId,
        receiverDisplayName = receiver?.displayName ?: receiver?.handle,
        receiverAvatarCdnImage = receiver?.avatarCdnImage,
        receiverPrimalLegendProfile = receiver?.primalPremiumInfo?.legendProfile,
        amountInSats = amountInSats.toDouble(),
        message = zapRequest.content,
        zappedEventId = noteId,
        zappedEventContent = zappedPost?.content,
        zappedEventNostrUris = nostrUris,
        zappedEventHashtags = zappedPost?.hashtags ?: emptyList(),
        createdAt = event.createdAt,
    )
}
