package ru.blays.ficbook.reader.shared.components.searchComponents.declaration

import com.arkivanov.decompose.value.Value
import ru.blays.ficbook.reader.shared.data.SearchParamsEntityShortcut

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