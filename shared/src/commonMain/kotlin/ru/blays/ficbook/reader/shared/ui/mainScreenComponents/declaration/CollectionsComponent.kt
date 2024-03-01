package ru.blays.ficbook.reader.shared.ui.mainScreenComponents.declaration

import com.arkivanov.decompose.value.Value
import ru.blays.ficbook.api.data.SectionWithQuery
import ru.blays.ficbook.reader.shared.data.dto.CollectionModelStable

interface CollectionsComponent {
    val state: Value<State>

    fun sendIntent(intent: Intent)

    fun onOutput(output: Output)

    sealed class Intent {
        data object Refresh: Intent()
    }

    sealed class Output {
        data class OpenCollection(val section: SectionWithQuery): Output() {
            constructor(
                name: String,
                href: String
            ) : this(
                section = SectionWithQuery(
                    name = name,
                    path = href,
                    queryParameters = null
                )
            )
        }
    }

    data class State(
        val list: List<CollectionModelStable> = emptyList(),
        val isLoading: Boolean = false,
        val isError: Boolean = false
    )
}

internal interface CollectionsComponentInternal: CollectionsComponent {
    fun refresh()
}