package ru.blays.ficbook.reader.shared.components.superfilterComponents

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.blays.ficbook.reader.shared.data.repo.declaration.IFiltersRepo

class SuperfilterFanficsTabComponent(
    componentContext: ComponentContext,
    filtersRepo: IFiltersRepo
): BaseSuperfilterTabComponent(
    componentContext,
    filtersRepo
) {
    override val state = filtersRepo.fanficsInBlacklist
        .map {
            SuperfilterTabComponent.State(
                it.map(SuperfilterTabComponent::BlacklistItem)
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
                    filtersRepo.removeFanficFromBlacklist(intent.value)
                }
            }
            SuperfilterTabComponent.Intent.ShowAddDialog -> Unit
        }
    }

    override fun childFactory(
        config: Unit,
        childContext: ComponentContext,
    ): SuperfilterTabComponent.AddValueDialogComponent {
        throw UnsupportedOperationException("This component doesn't have dialogs")
    }
}