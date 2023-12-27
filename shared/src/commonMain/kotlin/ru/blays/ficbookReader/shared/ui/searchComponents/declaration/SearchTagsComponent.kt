package ru.blays.ficbookReader.shared.ui.searchComponents.declaration

import com.arkivanov.decompose.value.Value
import ru.blays.ficbookReader.shared.data.dto.SearchedTagModel
import ru.blays.ficbookapi.dataModels.SearchedTagsModel

interface SearchTagsComponent {
    val state: Value<State>

    fun selectTag(select: Boolean, tag: SearchedTagModel)
    fun excludeTag(exclude: Boolean, tag: SearchedTagModel)

    fun changeSearchedName(name: String)
    fun clear()

    data class State(
        val searchedName: String,
        val searchedTags: Set<SearchedTagModel>,
        val selectedTags: Set<SearchedTagModel>,
        val excludedTags: Set<SearchedTagModel>
    )
}