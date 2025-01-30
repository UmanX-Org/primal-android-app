package net.primal.android.nostr.model

import kotlinx.serialization.Serializable

@Serializable
enum class NostrEventKind(val value: Int) {
    Metadata(value = 0),
    ShortTextNote(value = 1),
    RecommendRelay(value = 2),
    FollowList(value = 3),
    EncryptedDirectMessages(value = 4),
    EventDeletion(value = 5),
    ShortTextNoteRepost(value = 6),
    Reaction(value = 7),
    BadgeAward(value = 8),
    GenericRepost(value = 16),
    PictureNote(value = 20),
    ChannelCreation(value = 40),
    ChannelMetadata(value = 41),
    ChannelMessage(value = 42),
    ChannelHideMessage(value = 43),
    ChannelMuteUser(value = 44),
    FileMetadata(value = 1063),
    Reporting(value = 1984),
    ZapRequest(value = 9734),
    Zap(value = 9735),
    Highlight(value = 9802),
    MuteList(value = 10_000),
    PinList(value = 10_001),
    RelayListMetadata(value = 10_002),
    BookmarksList(value = 10_003),
    BlossomServerList(value = 10_063),
    WalletInfo(value = 13_194),
    ClientAuthentication(value = 22_242),
    WalletRequest(value = 23_194),
    WalletResponse(value = 23_195),
    NostrConnect(value = 24_133),
    CategorizedPeopleList(value = 30_000),
    CategorizedBookmarkList(value = 30_001),
    ProfileBadges(value = 30_008),
    BadgeDefinition(value = 30_009),
    LongFormContent(value = 30_023),
    ApplicationSpecificData(value = 30_078),
    AppRecommendation(value = 31_989),
    AppHandler(value = 31_990),
    PrimalEventStats(value = 10_000_100),
    PrimalNetStats(value = 10_000_101),
    PrimalExploreLegendCounts(value = 10_000_102),
    PrimalDefaultSettings(value = 10_000_103),
    PrimalUserProfileStats(value = 10_000_105),
    PrimalReferencedEvent(value = 10_000_107),
    PrimalUserScores(value = 10_000_108),
    PrimalRelays(value = 10_000_109),
    PrimalNotification(value = 10_000_110),
    PrimalNotificationsSeenUntil(value = 10_000_111),
    PrimalPaging(value = 10_000_113),
    PrimalMediaMapping(value = 10_000_114),
    PrimalEventUserStats(value = 10_000_115),
    PrimalDirectMessagesConversationsSummary(value = 10_000_118),
    PrimalCdnResource(value = 10_000_119),
    PrimalSimpleUploadRequest(value = 10_000_120),
    PrimalUploadResponse(value = 10_000_121),
    PrimalDefaultRelaysList(value = 10_000_124),
    PrimalIsUserFollowing(value = 10_000_125),
    PrimalLinkPreview(value = 10_000_128),
    PrimalNotificationsSummary2(value = 10_000_132),
    PrimalUserFollowersCounts(value = 10_000_133),
    PrimalDirectMessagesUnreadCount2(value = 10_000_134),
    PrimalChunkedUploadRequest(value = 10_000_135),
    PrimalUserRelaysList(value = 10_000_139),
    PrimalRelayHint(value = 10_000_141),
    PrimalLongFormWordsCount(value = 10_000_144),
    PrimalBroadcastResult(value = 10_000_149),
    PrimalLongFormContentFeeds(value = 10_000_152),
    PrimalSubSettings(value = 10_000_155),
    PrimalDvmFeedFollowsActions(value = 10_000_156),
    PrimalExplorePeopleNewFollowStats(value = 10_000_157),
    PrimalUserNames(value = 10_000_158),
    PrimalDvmFeedMetadata(value = 10_000_159),
    PrimalTrendingTopics(value = 10_000_160),
    PrimalClientConfig(value = 10_000_162),
    PrimalUserMediaStorageStats(value = 10_000_163),
    PrimalUserUploadInfo(value = 10_000_164),
    PrimalContentBroadcastStats(value = 10_000_166),
    PrimalContentBroadcastStatus(value = 10_000_167),
    PrimalLegendProfiles(value = 10_000_168),
    PrimalPremiumInfo(value = 10_000_169),
    PrimalWalletOperation(value = 10_000_300),
    PrimalWalletBalance(value = 10_000_301),
    PrimalWalletDepositInvoice(value = 10_000_302),
    PrimalWalletDepositLnUrl(value = 10_000_303),
    PrimalWalletTransactions(value = 10_000_304),
    PrimalWalletExchangeRate(value = 10_000_305),
    PrimalWalletIsUser(value = 10_000_306),
    PrimalWalletUserInfo(value = 10_000_307),
    PrimalWalletInAppPurchaseQuote(value = 10_000_308),
    PrimalWalletInAppPurchase(value = 10_000_309),
    PrimalWalletActivation(value = 10_000_311),
    PrimalWalletParsedLnUrl(value = 10_000_312),
    PrimalWalletParsedLnInvoice(value = 10_000_313),
    PrimalWalletMiningFees(value = 10_000_315),
    PrimalWalletOnChainAddress(value = 10_000_316),
    PrimalWalletUpdatedAt(value = 10_000_317),
    PrimalWalletNwcConnectionCreated(value = 10_000_319),
    PrimalWalletNwcConnectionList(value = 10_000_321),
    PrimalMembershipNameAvailable(value = 10_000_600),
    PrimalMembershipLegendPaymentInstructions(value = 10_000_601),
    PrimalMembershipPurchaseMonitor(value = 10_000_602),
    PrimalMembershipStatus(value = 10_000_603),
    PrimalMembershipHistory(value = 10_000_605),
    PrimalAppState(value = 10_000_999),
    PrimalLongFormContent(value = 10_030_023),
    Unknown(value = -1),
    ;

    companion object {
        fun valueOf(value: Int): NostrEventKind = enumValues<NostrEventKind>().find { it.value == value } ?: Unknown
    }
}
