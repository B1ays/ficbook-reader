package ru.blays.ficbookReader.shared.ui.commentsComponent.declaration

import com.arkivanov.decompose.value.Value
import ru.blays.ficbookReader.shared.data.dto.CommentBlockModelStable

interface WriteCommentComponent {
    val state: Value<State>

    fun editText(newText: String)

    fun addReply(block: CommentBlockModelStable)

    fun post()

    data class State(
        val text: String,
        val renderedBlocks: List<CommentBlockModelStable>,
        val followType: Int,
        val error: Boolean,
        val errorMessage: String?
    )
}