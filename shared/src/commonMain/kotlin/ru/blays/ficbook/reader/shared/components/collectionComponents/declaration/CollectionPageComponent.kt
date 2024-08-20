package ru.blays.ficbook.reader.shared.components.collectionComponents.declaration

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import ru.blays.ficbook.reader.shared.components.collectionComponents.implementation.EditCollectionComponent
import ru.blays.ficbook.reader.shared.components.collectionComponents.implementation.EditCollectionDialogConfig
import ru.blays.ficbook.reader.shared.components.fanficListComponents.declaration.FanficsListComponent
import ru.blays.ficbook.reader.shared.data.dto.CollectionPageModelStable

interface CollectionPageComponent {
    val state: Value<State>

    val fanficsListComponent: FanficsListComponent

    val editDialog: Value<ChildSlot<EditCollectionDialogConfig, EditCollectionComponent>>

    fun sendIntent(intent: Intent)

    @Serializable
    data class State(
        val collectionPage: CollectionPageModelStable?,
        val currentParams: SelectedSortParams,
        val loading: Boolean,
        val error: Boolean,
        val errorMessage: String?
    )

    sealed class Intent {
        data class ChangeSearchText(val searchText: String): Intent()
        data class ChangeFandom(val fandomCode: Pair<String, String>): Intent()
        data class ChangeDirection(val directionCode: Pair<String, String>): Intent()
        data class ChangeSortType(val sortTypeCode: Pair<String, String>): Intent()
        data object Search: Intent()
        data object ClearFilter: Intent()
        data object Refresh: Intent()
        data object Edit: Intent()
        data object Delete: Intent()
        data class ChangeSubscription(val follow: Boolean): Intent()
    }

    @Serializable
    data class SelectedSortParams(
        val searchText: String? = null,
        val fandom: Pair<String, String>? = null,
        val direction: Pair<String, String>? = null,
        val sort: Pair<String, String>? = null
    )
}