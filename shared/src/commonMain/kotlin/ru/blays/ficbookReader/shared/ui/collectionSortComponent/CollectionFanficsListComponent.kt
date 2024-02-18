package ru.blays.ficbookReader.shared.ui.collectionSortComponent

import com.arkivanov.decompose.value.Value
import ru.blays.ficbookReader.shared.data.dto.CollectionSortParamsStable
import ru.blays.ficbookReader.shared.ui.fanficListComponents.declaration.FanficsListComponent

interface CollectionFanficsListComponent {
    val state: Value<State>

    val fanficsListComponent: FanficsListComponent

    fun onIntent(intent: Intent)

    data class State(
        val collectionName: String,
        val availableParams: CollectionSortParamsStable?,
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
        data object Clear: Intent()
        data object Refresh: Intent()
    }

    data class SelectedSortParams(
        val searchText: String? = null,
        val fandom: Pair<String, String>? = null,
        val direction: Pair<String, String>? = null,
        val sort: Pair<String, String>? = null
    )
}