package ru.blays.ficbook.reader.shared.components.commentsComponent.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnStart
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.components.commentsComponent.declaration.CommentsComponent

class DefaultAllCommentsComponent(
    componentContext: ComponentContext,
    private val href: String,
    output: (CommentsComponent.Output) -> Unit
): BaseCommentsComponent(
    componentContext = componentContext,
    output = output
) {

    override fun loadNextPage() {
        if(!state.value.loading && hasNextPage) {
            coroutineScope.launch {
                _state.update {
                    it.copy(loading = true)
                }
                when(
                    val result = repository.getAll(href, nextPage)
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
                        nextPage++
                        hasNextPage = result.value.hasNextPage
                        _state.update {
                            it.copy(
                                loading = false,
                                error = false,
                                comments = it.comments + result.value.list,
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
            is CommentsComponent.Intent.LikeComment -> likeComment(intent.commentID, intent.newValue)
            is CommentsComponent.Intent.Refresh -> refresh()
            else -> Unit
        }
    }

    init {
        lifecycle.doOnStart(true) {
            val state = state.value
            if(state.comments.isEmpty() && !state.error) {
                loadNextPage()
            }
        }
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }
}