package ru.blays.ficbook.reader.shared.components.usersComponent.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnStart
import com.arkivanov.essenty.statekeeper.ExperimentalStateKeeperApi
import com.arkivanov.essenty.statekeeper.saveable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.serializer
import org.koin.mp.KoinPlatform
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.components.usersComponent.declaration.UsersFavouriteComponent
import ru.blays.ficbook.reader.shared.components.usersComponent.declaration.UsersRootComponent
import ru.blays.ficbook.reader.shared.data.repo.declaration.IUsersRepo

@OptIn(ExperimentalStateKeeperApi::class)
class DefaultUsersFavouriteComponent(
    componentContext: ComponentContext,
    private val output: (output: UsersRootComponent.Output) -> Unit
): UsersFavouriteComponent, ComponentContext by componentContext {
    private val repository: IUsersRepo by KoinPlatform.getKoin().inject()

    private val _state = MutableValue(
        UsersFavouriteComponent.State(
            list = emptyList(),
            loading = false,
            error = false,
            errorMessage = null,
        )
    )

    override val state get() = _state

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var hasNextPage: Boolean by saveable(
        serializer = Boolean.serializer(),
        key = HAS_NEXT_PAGE_KEY,
        init = { true }
    )
    private var nextPage: Int by saveable(
        serializer = Int.serializer(),
        key = NEXT_PAGE_KEY,
        init = { 1 }
    )

    override fun sendIntent(intent: UsersFavouriteComponent.Intent) {
        when(intent) {
            UsersFavouriteComponent.Intent.LoadNextPage -> loadNextPage()
        }
    }

    override fun onOutput(output: UsersRootComponent.Output) = this.output(output)

    private fun loadNextPage() {
        if(!state.value.loading && hasNextPage) {
            coroutineScope.launch {
                _state.update {
                    it.copy(loading = true)
                }
                when (
                    val result = repository.getFavouritesAuthors(nextPage)
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
                        hasNextPage = result.value.hasNextPage
                        nextPage++
                        _state.update {
                            it.copy(
                                loading = false,
                                error = false,
                                list = it.list + result.value.list
                            )
                        }
                    }
                }
            }
        }
    }

    init {
        lifecycle.doOnStart(true) {
            loadNextPage()
        }
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }

    companion object {
        private const val HAS_NEXT_PAGE_KEY = "has_next_page"
        private const val NEXT_PAGE_KEY = "next_page"
    }
}