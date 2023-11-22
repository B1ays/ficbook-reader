package ru.blays.ficbookReader.shared.ui.commentsComponent

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.blays.ficbookReader.shared.data.mappers.toStableModel
import ru.blays.ficbookapi.dataModels.CommentModel
import ru.blays.ficbookapi.ficbookConnection.IFicbookApi
import ru.blays.ficbookapi.result.ApiResult

class DefaultAllCommentsComponent(
    componentContext: ComponentContext,
    private val ficbookApi: IFicbookApi,
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
                val nextPage = page + 1
                when(
                    val result = ficbookApi.getComments(href, nextPage)
                ) {
                    is ApiResult.Error -> {
                        _state.update {
                            it.copy(
                                loading = false,
                                error = true,
                                errorMessage = result.message
                            )
                        }
                    }
                    is ApiResult.Success -> {
                        val value = result.value
                        page = nextPage
                        hasNextPage = value.hasNextPage
                        _state.update {
                            it.copy(
                                loading = false,
                                error = false,
                                comments = it.comments + value.comments.map(CommentModel::toStableModel),
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
}