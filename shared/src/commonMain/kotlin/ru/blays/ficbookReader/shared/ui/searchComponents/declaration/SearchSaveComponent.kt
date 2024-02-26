package ru.blays.ficbookReader.shared.ui.searchComponents.declaration

import com.arkivanov.decompose.value.Value
import ru.blays.ficbookReader.shared.data.dto.SearchParamsEntityShortcut

interface SearchSaveComponent {
    val state: Value<State>

    fun save(
        name: String,
        description: String
    )

    fun delete(shortcut: SearchParamsEntityShortcut)

    fun select(shortcut: SearchParamsEntityShortcut)

    fun update(
        shortcut: SearchParamsEntityShortcut,
        newName: String,
        newDescription: String,
        updateParams: Boolean
    )

    data class State(
        val saved: List<SearchParamsEntityShortcut>
    )
}