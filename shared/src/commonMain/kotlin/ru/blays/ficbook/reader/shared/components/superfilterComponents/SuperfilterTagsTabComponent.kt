package ru.blays.ficbook.reader.shared.components.superfilterComponents

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnStart
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.SearchedTagModel
import ru.blays.ficbook.reader.shared.data.repo.declaration.IFiltersRepo
import ru.blays.ficbook.reader.shared.data.repo.declaration.ISearchRepo
import kotlin.time.Duration.Companion.seconds

class SuperfilterTagsTabComponent(
    componentContext: ComponentContext,
    filtersRepo: IFiltersRepo,
    private val searchRepo: ISearchRepo
): BaseSuperfilterTabComponent(
    componentContext,
    filtersRepo
) {
    override val state = filtersRepo.tagsBlacklist
        .map {
            SuperfilterTabComponent.State(
                it.map { value ->
                    SuperfilterTabComponent.BlacklistItem(value = value)
                }
            )
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(1000),
            initialValue = SuperfilterTabComponent.State()
        )

    override fun onIntent(intent: SuperfilterTabComponent.Intent) {
        when(intent) {
            is SuperfilterTabComponent.Intent.Remove -> {
                scope.launch {
                    filtersRepo.removeTagFromBlacklist(intent.value)
                }
            }
            SuperfilterTabComponent.Intent.ShowAddDialog -> dialogNavigation.activate(Unit)
        }
    }

    override fun childFactory(
        config: Unit,
        childContext: ComponentContext,
    ): SuperfilterTabComponent.AddValueDialogComponent {
        return AddTagDialog(childContext)
    }

    @OptIn(FlowPreview::class)
    private inner class AddTagDialog(
        componentContext: ComponentContext
    ): SuperfilterTabComponent.AddValueDialogComponent(componentContext) {
        private val _state = MutableStateFlow(
            State(
                searchedName = "",
                values = emptyList()
            )
        )

        override val state: StateFlow<State>
            get() = _state.asStateFlow()

        override fun onSearchedNameChange(searchedName: String) {
            _state.update {
                it.copy(searchedName = searchedName)
            }
        }

        override fun select(value: String) {
            scope.launch {
                filtersRepo.addTagToBlacklist(value)
            }.invokeOnCompletion {
                close()
            }
        }

        override fun close() {
            dialogNavigation.dismiss()
        }

        init {
            var searchJob: Job? = null
            lifecycle.doOnStart(true) {
                searchJob = scope.launch {
                    state.map { it.searchedName }
                        .distinctUntilChanged()
                        .debounce(1.seconds)
                        .collect {
                            when(
                                val searchResult = searchRepo.findTags(it)
                            ) {
                                is ApiResult.Error -> Unit
                                is ApiResult.Success -> {
                                    val tags = searchResult.value.map(SearchedTagModel::title)
                                    _state.update { oldState ->
                                        oldState.copy(values = tags)
                                    }
                                }
                            }
                        }
                }
            }
            lifecycle.doOnDestroy {
                searchJob?.cancel()
            }
        }
    }
}