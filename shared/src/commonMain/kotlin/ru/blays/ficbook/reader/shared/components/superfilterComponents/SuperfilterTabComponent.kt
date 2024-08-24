package ru.blays.ficbook.reader.shared.components.superfilterComponents

import androidx.compose.runtime.Stable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.builtins.serializer
import org.koin.core.component.KoinComponent
import ru.blays.ficbook.reader.shared.data.repo.declaration.IFiltersRepo

interface SuperfilterTabComponent {
    val state: StateFlow<State>

    val addValueDialog: Value<ChildSlot<Unit, AddValueDialogComponent>>

    fun onIntent(intent: Intent)

    sealed class Intent {
        data object ShowAddDialog: Intent()
        data class Remove(val value: String): Intent()
    }

    @Stable
    @JvmInline
    value class State(val values: List<String> = emptyList())

    abstract class AddValueDialogComponent(
        componentContext: ComponentContext
    ): ComponentContext by componentContext {
        abstract val state: StateFlow<State>

        open val searchAvailable: Boolean = true

        abstract fun onSearchedNameChange(searchedName: String)
        abstract fun select(value: String)
        abstract fun close()

        @Stable
        data class State(
            val searchedName: String,
            val values: List<String>
        )
    }
}

abstract class BaseSuperfilterTabComponent(
    componentContext: ComponentContext,
    val filtersRepo: IFiltersRepo
): SuperfilterTabComponent, ComponentContext by componentContext, KoinComponent {
    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val dialogNavigation = SlotNavigation<Unit>()
    override val addValueDialog = childSlot(
        source = dialogNavigation,
        initialConfiguration = { null },
        handleBackButton = false,
        serializer = Unit.serializer(),
        childFactory = ::childFactory
    )

    abstract fun childFactory(
        config: Unit,
        childContext: ComponentContext
    ): SuperfilterTabComponent.AddValueDialogComponent
}