package ru.blays.ficbook.reader.shared.components.superfilterComponents

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.dismiss
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.blays.ficbook.reader.shared.data.dto.FanficDirection
import ru.blays.ficbook.reader.shared.data.repo.declaration.IFiltersRepo

internal class SuperfilterDirectionsTabComponent(
    componentContext: ComponentContext,
    filtersRepo: IFiltersRepo
): BaseSuperfilterTabComponent(
    componentContext,
    filtersRepo
) {
    override val state = filtersRepo.directionsBlacklist
        .map {
            SuperfilterTabComponent.State(
                it.map(FanficDirection::getForName)
                    .map(FanficDirection::direction)
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
                val enumName = FanficDirection.getForDirection(intent.value).name
                scope.launch {
                    filtersRepo.removeDirectionFromBlacklist(enumName)
                }
            }
            SuperfilterTabComponent.Intent.ShowAddDialog -> dialogNavigation.activate(Unit)
        }
    }

    override fun childFactory(
        config: Unit,
        childContext: ComponentContext,
    ): SuperfilterTabComponent.AddValueDialogComponent {
        return AddAuthorDialog(childContext)
    }

    private inner class AddAuthorDialog(
        componentContext: ComponentContext
    ): SuperfilterTabComponent.AddValueDialogComponent(componentContext) {
        val allDirections = FanficDirection.entries - FanficDirection.UNKNOWN
        val directionsInBlacklist = filtersRepo.directionsBlacklist.value.map(FanficDirection::getForName)
        val directionsNames = (allDirections - directionsInBlacklist.toSet()).map(FanficDirection::direction)

        override val state: StateFlow<State> = MutableStateFlow(
            State(
                searchedName = "",
                values = directionsNames
            )
        )

        override val searchAvailable: Boolean = false

        override fun onSearchedNameChange(searchedName: String) = Unit

        override fun select(value: String) {
            scope.launch {
                val enumName = FanficDirection.getForDirection(value).name
                filtersRepo.addDirectionToBlacklist(enumName)
            }.invokeOnCompletion {
                close()
            }
        }

        override fun close() {
            dialogNavigation.dismiss()
        }
    }
}