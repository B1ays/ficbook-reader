package ru.blays.ficbook.reader.shared.components.searchComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.components.Utils.ExternalStateUpdatable
import ru.blays.ficbook.reader.shared.components.searchComponents.declaration.SearchFandomsComponent
import ru.blays.ficbook.reader.shared.data.SearchedFandomModel
import ru.blays.ficbook.reader.shared.data.repo.declaration.ISearchRepo
import ru.blays.ficbook.reader.shared.stateHandle.SaveableMutableValue
import kotlin.time.Duration.Companion.seconds

class DefaultSearchFandomsComponent(
    componentContext: ComponentContext
): SearchFandomsComponent,
    ExternalStateUpdatable<SearchFandomsComponent.State>,
    ComponentContext by componentContext,
    KoinComponent {
    private val repository: ISearchRepo by inject()

    private val _state = SaveableMutableValue(
        serializer = SearchFandomsComponent.State.serializer(),
        initialValue = SearchFandomsComponent.State(
            searchedName = "",
            searchedFandoms = emptySet(),
            selectedFandoms = emptySet(),
            excludedFandoms = emptySet()
        )
    )

    private var searchJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override val state: Value<SearchFandomsComponent.State> get() = _state

    override fun selectFandom(select: Boolean, fandom: SearchedFandomModel) {
        if(select) {
            _state.update {
                it.copy(
                    selectedFandoms = it.selectedFandoms + fandom
                )
            }
        } else {
            _state.update {
                it.copy(
                    selectedFandoms = it.selectedFandoms - fandom
                )
            }
        }
    }

    override fun excludeFandom(exclude: Boolean, fandom: SearchedFandomModel) {
        if(exclude) {
            _state.update {
                it.copy(
                    excludedFandoms = it.excludedFandoms + fandom
                )
            }
        } else {
            _state.update {
                it.copy(
                    excludedFandoms = it.excludedFandoms - fandom
                )
            }
        }
    }

    override fun changeSearchedName(name: String) = search(name)

    override fun clear() {
        _state.update {
            it.copy(
                searchedName = "",
                searchedFandoms = emptySet(),
            )
        }
    }

    override fun updateState(block: (SearchFandomsComponent.State) -> SearchFandomsComponent.State) {
        _state.update(block)
    }

    private fun search(name: String) {
        searchJob?.cancel()
        _state.update {
            it.copy(searchedName = name)
        }
        if(name.isNotEmpty()) {
            searchJob = coroutineScope.launch {
                delay(0.7.seconds)
                if(searchJob?.isActive == true) {
                    when(
                        val result = repository.findFandoms(name)
                    ) {
                        is ApiResult.Error -> {}
                        is ApiResult.Success -> {
                            _state.update {
                                it.copy(
                                    searchedFandoms = result.value.toSet()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}