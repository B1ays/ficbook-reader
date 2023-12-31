package ru.blays.ficbookReader.shared.ui.commentsComponent.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import ru.blays.ficbookReader.shared.ui.commentsComponent.declaration.CommentsComponent

abstract class BaseCommentsComponent(
    componentContext: ComponentContext,
    private val output: (CommentsComponent.Output) -> Unit
): CommentsComponent, ComponentContext by componentContext {
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

    fun refresh() {
        _state.update {
            it.copy(comments = emptyList())
        }
        nextPage = 1
        loadNextPage()
    }
}