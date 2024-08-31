package ru.blays.ficbook.reader.shared.components.commentsComponent.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.statekeeper.ExperimentalStateKeeperApi
import com.arkivanov.essenty.statekeeper.saveable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.serializer
import org.koin.mp.KoinPlatform
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.components.commentsComponent.declaration.CommentsComponent
import ru.blays.ficbook.reader.shared.data.repo.declaration.ICommentsRepo
import ru.blays.ficbook.reader.shared.stateHandle.SaveableMutableValue

@OptIn(ExperimentalStateKeeperApi::class)
abstract class BaseCommentsComponent(
    componentContext: ComponentContext,
    private val output: (CommentsComponent.Output) -> Unit
): CommentsComponent, ComponentContext by componentContext {
    val repository: ICommentsRepo = KoinPlatform.getKoin().get()

    internal val _state = SaveableMutableValue(
        serializer = CommentsComponent.State.serializer(),
        initialValue = CommentsComponent.State(
            loading = false,
            error = false,
            errorMessage = null,
            comments = emptyList()
        )
    )
    override val state get() = _state

    internal val coroutineScope = CoroutineScope(Dispatchers.IO)

    internal var hasNextPage: Boolean by saveable(
        serializer = Boolean.serializer(),
        key = HAS_NEXT_PAGE_KEY,
        init = { true }

    )
    internal var nextPage: Int by saveable(
        serializer = Int.serializer(),
        key = NEXT_PAGE_KEY,
        init = { 1 }
    )

    override fun onOutput(output: CommentsComponent.Output) {
        output(output)
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

    fun likeComment(commentID: String, like: Boolean) {
        coroutineScope.launch {
            when(
                val result = repository.like(commentID, like)
            ) {
                is ApiResult.Error -> Unit
                is ApiResult.Success -> {
                    val success = result.value
                    if(success) {
                        _state.update { oldState ->
                            val newComments = oldState.comments.map {
                                if(it.commentID == commentID) {
                                    it.copy(
                                        isLiked = like,
                                        likes = if(like) it.likes + 1 else it.likes - 1
                                    )
                                } else {
                                    it
                                }
                            }
                            oldState.copy(comments = newComments)
                        }
                    }
                }
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

    companion object {
        private const val HAS_NEXT_PAGE_KEY = "has_next_page"
        private const val NEXT_PAGE_KEY = "next_page"
    }
}