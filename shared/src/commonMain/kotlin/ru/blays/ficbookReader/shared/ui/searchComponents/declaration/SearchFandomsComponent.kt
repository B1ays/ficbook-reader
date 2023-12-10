package ru.blays.ficbookReader.shared.ui.searchComponents.declaration

import com.arkivanov.decompose.value.Value
import ru.blays.ficbookReader.shared.data.dto.SearchedFandomsModel

interface SearchFandomsComponent {
    val state: Value<State>

    fun selectFandom(
        select: Boolean,
        fandom: SearchedFandomsModel.Data.Result
    )
    fun excludeFandom(
        exclude: Boolean,
        fandom: SearchedFandomsModel.Data.Result
    )
    fun changeSearchedName(name: String)
    fun clear()

    data class State(
        val searchedName: String,
        val searchedFandoms: Set<SearchedFandomsModel.Data.Result>,
        val selectedFandoms: Set<SearchedFandomsModel.Data.Result>,
        val excludedFandoms: Set<SearchedFandomsModel.Data.Result>
    )
}