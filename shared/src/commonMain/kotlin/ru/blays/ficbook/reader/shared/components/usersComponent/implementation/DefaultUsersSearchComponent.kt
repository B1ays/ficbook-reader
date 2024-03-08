package ru.blays.ficbook.reader.shared.components.usersComponent.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.update
import kotlinx.coroutines.*
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.repo.declaration.IUsersRepo
import ru.blays.ficbook.reader.shared.components.usersComponent.declaration.UsersRootComponent
import ru.blays.ficbook.reader.shared.components.usersComponent.declaration.UsersSearchComponent
import kotlin.time.Duration.Companion.milliseconds

class DefaultUsersSearchComponent(
    componentContext: ComponentContext,
    private val output: (output: UsersRootComponent.Output) -> Unit
): UsersSearchComponent, ComponentContext by componentContext {
    private val repository: IUsersRepo by getKoin().inject()

    private val _state = MutableValue(
        UsersSearchComponent.State(
            searchedName = "",
            list = emptyList(),
            loading = false,
            error = false,
            errorMessage = null
        )
    )

    override val state get() = _state

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var searchUsersJob: Job? = null

    override fun sendIntent(intent: UsersSearchComponent.Intent) {
        when(intent) {
            is UsersSearchComponent.Intent.ChangeSearchedName -> searchNewName(intent.newName)
            UsersSearchComponent.Intent.Clear -> clear()
        }
    }

    override fun onOutput(output: UsersRootComponent.Output) = this.output(output)

    private fun clear() {
        searchUsersJob?.cancel()
        _state.update {
            it.copy(
                searchedName = "",
                list = emptyList(),
                loading = false,
                error = false,
                errorMessage = null
            )
        }
    }

    private fun searchNewName(name: String) {
        _state.update {
            it.copy(searchedName = name)
        }
        searchUsersJob?.cancel()
        if(name.isNotEmpty()) {
            searchUsersJob = coroutineScope.launch {
                delay(500.milliseconds)
                if(searchUsersJob?.isActive == true) {
                    search(name)
                }
            }
        }
    }

    private suspend fun search(name: String) {
        _state.update {
            it.copy(loading = true)
        }
        when(
            val result = repository.searchAuthor(name, 1)
        ) {
            is ApiResult.Error -> {
                _state.update {
                    it.copy(
                        errorMessage = result.exception.message,
                        error = true,
                        loading = false
                    )
                }
            }
            is ApiResult.Success -> {
                _state.update {
                    it.copy(
                        list = result.value,
                        loading = false,
                        error = false,
                        errorMessage = null
                    )
                }
            }
        }
    }
}