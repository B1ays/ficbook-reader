package ru.blays.ficbookReader.shared.ui.searchComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import kotlinx.coroutines.*
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbookReader.shared.data.dto.SearchedFandomsModel
import ru.blays.ficbookReader.shared.data.repo.declaration.ISearchRepo
import ru.blays.ficbookReader.shared.ui.searchComponents.declaration.SearchFandomsComponent
import ru.blays.ficbookapi.result.ApiResult
import kotlin.time.Duration.Companion.seconds

class DefaultSearchFandomsComponent(
    componentContext: ComponentContext
): SearchFandomsComponent, ComponentContext by componentContext {
    private val repository: ISearchRepo by getKoin().inject()

    private val _state = MutableValue(
        SearchFandomsComponent.State(
            searchedName = "",
            searchedFandoms = emptySet(),
            selectedFandoms = emptySet(),
            excludedFandoms = emptySet()
        )
    )

    private var searchJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override val state: Value<SearchFandomsComponent.State> get() = _state

    override fun selectFandom(select: Boolean, fandom: SearchedFandomsModel.Data.Result) {
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

    override fun excludeFandom(exclude: Boolean, fandom: SearchedFandomsModel.Data.Result) {
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

    private fun search(name: String) {
        searchJob?.cancel()
        _state.update {
            it.copy(searchedName = name)
        }
        if(name.isNotEmpty()) {
            searchJob = coroutineScope.launch {
                delay(1.seconds)
                if(searchJob?.isActive == true) {
                    when(
                        val result = repository.findFandoms(name)
                    ) {
                        is ApiResult.Error -> {}
                        is ApiResult.Success -> {
                            _state.update {
                                it.copy(
                                    searchedFandoms = result.value.data.result.toSet()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}