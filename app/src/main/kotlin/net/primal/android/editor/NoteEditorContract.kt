package net.primal.android.editor

import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import java.util.*
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.profile.model.UserProfileItemUi
import net.primal.android.editor.domain.NoteAttachment
import net.primal.android.editor.domain.NoteTaggedUser

interface NoteEditorContract {

    data class UiState(
        val content: TextFieldValue,
        val conversation: List<FeedPostUi> = emptyList(),
        val publishing: Boolean = false,
        val error: NoteEditorError? = null,
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val uploadingAttachments: Boolean = false,
        val attachments: List<NoteAttachment> = emptyList(),
        val taggedUsers: List<NoteTaggedUser> = emptyList(),
        val userTaggingQuery: String? = null,
        val users: List<UserProfileItemUi> = emptyList(),
        val recommendedUsers: List<UserProfileItemUi> = emptyList(),
    ) {
        val isReply: Boolean get() = conversation.isNotEmpty()
        val replyToNote: FeedPostUi? = conversation.lastOrNull()

        sealed class NoteEditorError {
            data class PublishError(val cause: Throwable?) : NoteEditorError()
            data class MissingRelaysConfiguration(val cause: Throwable) : NoteEditorError()
        }
    }

    sealed class UiEvent {
        data class UpdateContent(val content: TextFieldValue) : UiEvent()
        data object PublishNote : UiEvent()
        data class ImportLocalFiles(val uris: List<Uri>) : UiEvent()
        data class DiscardNoteAttachment(val attachmentId: UUID) : UiEvent()
        data class RetryUpload(val attachmentId: UUID) : UiEvent()
        data class SearchUsers(val query: String) : UiEvent()
        data class ToggleSearchUsers(val enabled: Boolean) : UiEvent()
        data class TagUser(val taggedUser: NoteTaggedUser) : UiEvent()
    }

    sealed class SideEffect {
        data object PostPublished : SideEffect()
    }
}
