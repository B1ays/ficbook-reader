package ru.blays.ficbookReader.shared.ui.searchComponents.declaration

import com.arkivanov.decompose.value.Value
import ru.blays.ficbookReader.shared.data.dto.SearchedTagsModel

interface SearchTagsComponent {
    val state: Value<State>

    fun selectTag(select: Boolean, tag: SearchedTagsModel.Data.Tag)
    fun excludeTag(exclude: Boolean, tag: SearchedTagsModel.Data.Tag)

    fun changeSearchedName(name: String)
    fun clear()

    data class State(
        val searchedName: String,
        val searchedTags: Set<SearchedTagsModel.Data.Tag>,
        val selectedTags: Set<SearchedTagsModel.Data.Tag>,
        val excludedTags: Set<SearchedTagsModel.Data.Tag>
    )
}