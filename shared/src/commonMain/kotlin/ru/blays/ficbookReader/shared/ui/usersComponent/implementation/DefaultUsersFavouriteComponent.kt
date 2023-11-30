package ru.blays.ficbookReader.shared.ui.usersComponent.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform
import ru.blays.ficbookReader.shared.data.repo.declaration.IUsersRepo
import ru.blays.ficbookReader.shared.ui.usersComponent.declaration.UsersFavouriteComponent
import ru.blays.ficbookReader.shared.ui.usersComponent.declaration.UsersRootComponent
import ru.blays.ficbookapi.result.ApiResult

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

    private var currentPage: Int = 0
    private var hasNextPage: Boolean = true

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
                val nextPage = currentPage + 1
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
                        this@DefaultUsersFavouriteComponent.hasNextPage = result.value.hasNextPage
                        this@DefaultUsersFavouriteComponent.currentPage = nextPage
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
        lifecycle.doOnCreate {
            loadNextPage()
        }
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }
}