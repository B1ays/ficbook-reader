package ru.blays.ficbook.reader.shared.components.searchComponents.declaration

import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import ru.blays.ficbook.reader.shared.data.SearchedTagModel

interface SearchTagsComponent {
    val state: Value<State>

    fun selectTag(select: Boolean, tag: SearchedTagModel)
    fun excludeTag(exclude: Boolean, tag: SearchedTagModel)
    fun changeSearchedName(name: String)
    fun changeSearchBehavior(behavior: Int)
    fun clear()

    @Serializable
    data class State(
        val searchedName: String,
        val searchedTags: Set<SearchedTagModel>,
        val selectedTags: Set<SearchedTagModel>,
        val excludedTags: Set<SearchedTagModel>,
        val behavior: Int
    )
}