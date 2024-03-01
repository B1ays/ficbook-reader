package ru.blays.ficbook.reader.shared.ui.searchComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.update
import kotlinx.coroutines.*
import org.koin.mp.KoinPlatform
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.dto.SearchedTagModel
import ru.blays.ficbook.reader.shared.data.repo.declaration.ISearchRepo
import ru.blays.ficbook.reader.shared.ui.Utils.ExternalStateUpdatable
import ru.blays.ficbook.reader.shared.ui.searchComponents.declaration.SearchTagsComponent
import kotlin.time.Duration.Companion.seconds

class DefaultSearchTagsComponent(
    componentContext: ComponentContext
): SearchTagsComponent,
    ExternalStateUpdatable<SearchTagsComponent.State>,
    ComponentContext by componentContext {
    private val repository: ISearchRepo by KoinPlatform.getKoin().inject()

    private val _state = MutableValue(
        SearchTagsComponent.State(
            searchedName = "",
            searchedTags = emptySet(),
            selectedTags = emptySet(),
            excludedTags = emptySet()
        )
    )

    private var searchJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override val state get() = _state


    override fun selectTag(
        select: Boolean,
        tag: SearchedTagModel
    ) {
        if(select) {
            _state.update {
                it.copy(
                    selectedTags = it.selectedTags + tag
                )
            }
        } else {
            _state.update {
                it.copy(
                    selectedTags = it.selectedTags - tag
                )
            }
        }
    }

    override fun excludeTag(
        exclude: Boolean,
        tag: SearchedTagModel
    ) {
        if(exclude) {
            _state.update {
                it.copy(
                    excludedTags = it.excludedTags + tag
                )
            }
        } else {
            _state.update {
                it.copy(
                    excludedTags = it.excludedTags - tag
                )
            }
        }
    }

    override fun changeSearchedName(name: String) = search(name)

    override fun clear() {
        _state.update {
            it.copy(
                searchedName = "",
                searchedTags = emptySet()
            )
        }
    }

    override fun updateState(block: (SearchTagsComponent.State) -> SearchTagsComponent.State) {
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
                        val result = repository.findTags(name)
                    ) {
                        is ApiResult.Error -> {}
                        is ApiResult.Success -> {
                            _state.update {
                                it.copy(
                                    searchedTags = result.value.toSet()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}