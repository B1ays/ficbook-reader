package ru.blays.ficbookReader.shared.ui.commentsComponent.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbookReader.shared.data.dto.CommentBlockModelStable
import ru.blays.ficbookReader.shared.data.dto.QuoteModelStable
import ru.blays.ficbookReader.shared.data.repo.declaration.ICommentsRepo
import ru.blays.ficbookReader.shared.ui.commentsComponent.declaration.CommentsComponent
import ru.blays.ficbookReader.shared.ui.commentsComponent.declaration.WriteCommentComponent
import ru.blays.ficbookapi.result.ApiResult

class DefaultPartCommentsComponent(
    componentContext: ComponentContext,
    private val partID: String,
    output: (CommentsComponent.Output) -> Unit
): BaseCommentsComponent(
    componentContext = componentContext,
    output = output
) {
    private val repository: ICommentsRepo = getKoin().get()

    val writeCommentComponent: WriteCommentComponent = DefaultWriteCommentComponent(
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
                block = intent.block
            )
        }
    }

    private fun addReply(
        userName: String,
        block: CommentBlockModelStable
    ) {
        val newQuote = QuoteModelStable(
            quote = block.quote,
            userName = userName,
            text = block.text
        )
        val newBlock = CommentBlockModelStable(
            quote = newQuote,
            text = ""
        )
        writeCommentComponent.addReply(newBlock)
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