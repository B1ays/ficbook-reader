package ru.blays.ficbook.reader.shared.components.commentsComponent.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
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
                        val value = result.value
                        this@DefaultAllCommentsComponent.nextPage += 1
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
            else -> Unit
        }
    }

    init {
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
        loadNextPage()
    }
}