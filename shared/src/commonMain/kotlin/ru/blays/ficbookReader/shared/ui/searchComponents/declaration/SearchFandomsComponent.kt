package ru.blays.ficbookReader.shared.ui.searchComponents.declaration

import com.arkivanov.decompose.value.Value
import ru.blays.ficbookReader.shared.data.dto.SearchedFandomModel
import ru.blays.ficbookapi.dataModels.SearchedFandomsModel

interface SearchFandomsComponent {
    val state: Value<State>

    fun selectFandom(
        select: Boolean,
        fandom: SearchedFandomModel
    )
    fun excludeFandom(
        exclude: Boolean,
        fandom: SearchedFandomModel
    )
    fun changeSearchedName(name: String)
    fun clear()

    data class State(
        val searchedName: String,
        val searchedFandoms: Set<SearchedFandomModel>,
        val selectedFandoms: Set<SearchedFandomModel>,
        val excludedFandoms: Set<SearchedFandomModel>
    )
}