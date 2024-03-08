package ru.blays.ficbook.reader.shared.components.commentsComponent.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.repo.declaration.ICommentsRepo
import ru.blays.ficbook.reader.shared.components.commentsComponent.declaration.CommentsComponent

abstract class BaseCommentsComponent(
    componentContext: ComponentContext,
    private val output: (CommentsComponent.Output) -> Unit
): CommentsComponent, ComponentContext by componentContext {
    val repository: ICommentsRepo = KoinPlatform.getKoin().get()

    internal val _state = MutableValue(
        CommentsComponent.State(
            loading = false,
            error = false,
            errorMessage = null,
            comments = emptyList()
        )
    )
    override val state get() = _state

    internal val coroutineScope = CoroutineScope(Dispatchers.IO)

    internal var hasNextPage: Boolean = true
    internal var nextPage: Int = 1

    override fun onOutput(output: CommentsComponent.Output) {
        this.output(output)
    }

    abstract fun loadNextPage()

    fun deleteComment(commentID: String) {
        coroutineScope.launch {
            when(repository.delete(commentID)) {
                is ApiResult.Error -> Unit
                is ApiResult.Success -> refresh()

            }
        }
    }

    fun refresh() {
        _state.update {
            it.copy(comments = emptyList())
        }
        hasNextPage = true
        nextPage = 1
        loadNextPage()
    }
}