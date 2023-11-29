package ru.blays.ficbookReader.shared.ui.fanficPageComponents.declaration

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import ru.blays.ficbookReader.shared.data.dto.AvailableCollectionsModel

interface FanficPageActionsComponent {
    val state: Value<State>

    val slot: Value<ChildSlot<*, FanficPageCollectionsComponent>>

    fun sendIntent(intent: Intent)
    fun onOutput(output: Output)

    @Serializable
    data class ChildConfig(val fanficId: String)

    sealed class Intent {
        data object OpenAvailableCollections : Intent()
        data object CloseAvailableCollections : Intent()
        data class Follow(val follow: Boolean): Intent()
        data class Mark(val mark: Boolean): Intent()
    }

    sealed class Output {
        data object OpenComments: Output()
    }

    data class State(
        val follow: Boolean = false,
        val mark: Boolean = false
    )
}

interface InternalFanficPageActionsComponent: FanficPageActionsComponent {
    fun setFanficID(id: String)
    fun setValue(value: FanficPageActionsComponent.State)
}

interface FanficPageCollectionsComponent {
    val state: Value<State>

    fun sendIntent(intent: Intent)

    sealed class Intent {
        data class AddToCollection(
            val add: Boolean,
            val collection: AvailableCollectionsModel.Data.Collection
        ): Intent()
    }

    data class State(
        val availableCollections: AvailableCollectionsModel?,
        val loading: Boolean,
        val error: Boolean,
        val errorMessage: String?
    )
}
