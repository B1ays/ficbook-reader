package ru.blays.ficbook.reader.shared.components.searchComponents.declaration

import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import ru.blays.ficbook.reader.shared.data.SearchedFandomModel

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

    @Serializable
    data class State(
        val searchedName: String,
        val searchedFandoms: Set<SearchedFandomModel>,
        val selectedFandoms: Set<SearchedFandomModel>,
        val excludedFandoms: Set<SearchedFandomModel>
    )
}