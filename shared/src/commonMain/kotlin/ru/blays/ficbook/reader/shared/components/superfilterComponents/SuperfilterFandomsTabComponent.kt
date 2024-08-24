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
import ru.blays.ficbook.reader.shared.data.SearchedFandomModel
import ru.blays.ficbook.reader.shared.data.repo.declaration.IFiltersRepo
import ru.blays.ficbook.reader.shared.data.repo.declaration.ISearchRepo
import kotlin.time.Duration.Companion.seconds

class SuperfilterFandomsTabComponent(
    componentContext: ComponentContext,
    filtersRepo: IFiltersRepo,
    private val searchRepo: ISearchRepo
): BaseSuperfilterTabComponent(
    componentContext,
    filtersRepo
) {
    override val state = filtersRepo.fandomsBlacklist
        .map(SuperfilterTabComponent::State)
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(1000),
            initialValue = SuperfilterTabComponent.State()
        )

    override fun onIntent(intent: SuperfilterTabComponent.Intent) {
        when(intent) {
            is SuperfilterTabComponent.Intent.Remove -> {
                scope.launch {
                    filtersRepo.removeFandomFromBlacklist(intent.value)
                }
            }
            SuperfilterTabComponent.Intent.ShowAddDialog -> dialogNavigation.activate(Unit)
        }
    }

    override fun childFactory(
        config: Unit,
        childContext: ComponentContext,
    ): SuperfilterTabComponent.AddValueDialogComponent {
        return AddFandomDialog(childContext)
    }

    @OptIn(FlowPreview::class)
    private inner class AddFandomDialog(
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
                filtersRepo.addFandomToBlacklist(value)
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
                                val searchResult = searchRepo.findFandoms(it)
                            ) {
                                is ApiResult.Error -> Unit
                                is ApiResult.Success -> {
                                    val fandoms = searchResult.value.map(SearchedFandomModel::title)
                                    _state.update { oldState ->
                                        oldState.copy(values = fandoms)
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