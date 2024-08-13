package ru.blays.ficbook.reader.shared.components.commentsComponent.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.components.commentsComponent.declaration.CommentsComponent
import ru.blays.ficbook.reader.shared.components.commentsComponent.declaration.ExtendedCommentsComponent
import ru.blays.ficbook.reader.shared.data.dto.CommentBlockModelStable
import ru.blays.ficbook.reader.shared.data.dto.QuoteModelStable

class DefaultPartCommentsComponent(
    componentContext: ComponentContext,
    private val partID: String,
    output: (CommentsComponent.Output) -> Unit
): BaseCommentsComponent(
    componentContext = componentContext,
    output = output
), ExtendedCommentsComponent {

    override val writeCommentComponent = DefaultWriteCommentComponent(
        componentContext = componentContext,
        partID = partID,
        onCommentPosted = ::refresh
    )

    override fun loadNextPage() {
        if(!state.value.loading && hasNextPage) {
            coroutineScope.launch {
                _state.update {
                    it.copy(loading = true)
                }
                when(
                    val result = repository.getForPart(
                        partID = partID,
                        page = nextPage
                    )
                ) {
                    is ApiResult.Error -> {
                        _state.update {
                            it.copy(
                                loading = false,
                                error = true,
                                errorMessage = result.exception.message
                            )
                        }
                    }
                    is ApiResult.Success -> {
                        val value = result.value
                        this@DefaultPartCommentsComponent.nextPage += 1
                        hasNextPage = value.hasNextPage
                        _state.update {
                            it.copy(
                                loading = false,
                                error = false,
                                comments = it.comments + value.list,
                            )
                        }
                    }
                }
            }
        }
    }

    override fun sendIntent(intent: CommentsComponent.Intent) {
        when(intent) {
            is CommentsComponent.Intent.LoadNextPage -> loadNextPage()
            is CommentsComponent.Intent.AddReply -> addReply(
                userName = intent.userName,
                blocks = intent.blocks
            )
            is CommentsComponent.Intent.LikeComment -> likeComment(intent.commentID, intent.newValue)
            is CommentsComponent.Intent.DeleteComment -> deleteComment(intent.commentID)
            CommentsComponent.Intent.Refresh -> refresh()
        }
    }

    private fun addReply(
        userName: String,
        blocks: List<CommentBlockModelStable>
    ) {
        val newBlocks = blocks.mapIndexed { index, block ->
            val newQuote = if(index == 0) {
                QuoteModelStable(
                    quote = block.quote,
                    userName = userName,
                    text = block.text
                )
            } else {
                QuoteModelStable(
                    quote = block.quote,
                    userName = "",
                    text = block.text
                )
            }
            CommentBlockModelStable(
                quote = newQuote,
                text = ""
            )
        }
        writeCommentComponent.addReply(newBlocks)
    }

    init {
        lifecycle.doOnDestroy {
            coroutineScope.launch {
                coroutineScope.cancel()
            }
        }
        loadNextPage()
    }
}