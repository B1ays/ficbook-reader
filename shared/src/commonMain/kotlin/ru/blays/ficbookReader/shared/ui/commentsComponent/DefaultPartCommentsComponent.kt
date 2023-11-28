package ru.blays.ficbookReader.shared.ui.commentsComponent

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbookReader.shared.data.repo.declaration.ICommentsRepo
import ru.blays.ficbookapi.result.ApiResult

class DefaultPartCommentsComponent(
    componentContext: ComponentContext,
    private val partID: String,
    output: (CommentsComponent.Output) -> Unit
): BaseCommentsComponent(
    componentContext = componentContext,
    output = output
) {
    val repository: ICommentsRepo = getKoin().get()

    override fun loadNextPage() {
        if(!state.value.loading && hasNextPage) {
            coroutineScope.launch {
                _state.update {
                    it.copy(loading = true)
                }
                val nextPage = currentPage + 1
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
                        currentPage = nextPage
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

    init {
        lifecycle.doOnDestroy {
            coroutineScope.launch {
                coroutineScope.cancel()
            }
        }
        loadNextPage()
    }

    companion object {
        fun createWithHref(
            componentContext: ComponentContext,
            href: String,
            output: (CommentsComponent.Output) -> Unit
        ): DefaultPartCommentsComponent {
            val partID = href.substringAfterLast('/').substringBefore('?')
            return DefaultPartCommentsComponent(
                componentContext = componentContext,
                partID = partID,
                output = output
            )
        }
    }
}