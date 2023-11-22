package ru.blays.ficbookReader.shared.ui.commentsComponent

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

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
    internal var page: Int = 0

    override fun sendIntent(intent: CommentsComponent.Intent) {
        when(intent) {
            CommentsComponent.Intent.LoadNextPage -> loadNextPage()
        }
    }

    override fun onOutput(output: CommentsComponent.Output) {
        this.output(output)
    }

    abstract fun loadNextPage()

}