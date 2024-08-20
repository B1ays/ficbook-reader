package ru.blays.ficbook.reader.shared.components.commentsComponent.declaration

import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import ru.blays.ficbook.reader.shared.data.dto.CommentBlockModelStable
import ru.blays.ficbook.reader.shared.data.dto.CommentModelStable

interface CommentsComponent {
    val state: Value<State>

    fun sendIntent(intent: Intent)
    fun onOutput(output: Output)

    sealed class Intent {
        data object LoadNextPage: Intent()
        data object Refresh : Intent()
        data class AddReply(val userName: String, val blocks: List<CommentBlockModelStable>): Intent()
        class DeleteComment(val commentID: String) : Intent()
        data class LikeComment(val commentID: String, val newValue: Boolean) : Intent()
    }

    sealed class Output {
        data object NavigateBack: Output()
        data class OpenAuthor(val href: String) : Output()
        data class OpenUrl(val url: String): Output()
        class OpenFanfic(val href: String) : Output()
    }

    @Serializable
    data class State(
        val loading: Boolean,
        val error: Boolean,
        val errorMessage: String?,
        val comments: List<CommentModelStable>
    )
}

interface ExtendedCommentsComponent: CommentsComponent {
    val writeCommentComponent: WriteCommentComponent
}