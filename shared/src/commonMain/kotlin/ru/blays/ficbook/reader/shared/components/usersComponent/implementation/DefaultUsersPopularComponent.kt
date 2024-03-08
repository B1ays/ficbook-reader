package ru.blays.ficbook.reader.shared.components.usersComponent.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.repo.declaration.IUsersRepo
import ru.blays.ficbook.reader.shared.components.usersComponent.declaration.UsersPopularComponent
import ru.blays.ficbook.reader.shared.components.usersComponent.declaration.UsersRootComponent

class DefaultUsersPopularComponent(
    componentContext: ComponentContext,
    private val output: (output: UsersRootComponent.Output) -> Unit
): UsersPopularComponent, ComponentContext by componentContext {
    private val repository: IUsersRepo by getKoin().inject()

    private val _state = MutableValue(
        UsersPopularComponent.State(
            list = emptyList(),
            loading = false,
            error = false,
            errorMessage = null,
        )
    )

    override val state get() = _state

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onOutput(output: UsersRootComponent.Output) = this.output(output)

    private fun loadAuthors() {
        coroutineScope.launch {
            _state.update {
                it.copy(loading = true)
            }
            when(
                val result = repository.getPopularAuthors()
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
                    _state.update {
                        it.copy(
                            loading = false,
                            error = false,
                            list = result.value
                        )
                    }
                }
            }
        }
    }

    init {
        loadAuthors()
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }
}