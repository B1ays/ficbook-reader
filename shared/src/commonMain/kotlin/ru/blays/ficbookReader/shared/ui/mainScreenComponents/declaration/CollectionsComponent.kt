package ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration

import com.arkivanov.decompose.value.Value
import ru.blays.ficbookReader.shared.data.dto.CollectionModelStable
import ru.blays.ficbookapi.data.SectionWithQuery

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
                href: String
            ) : this(
                SectionWithQuery(
                    path = href,
                    name = "",
                    queryParameters = emptyList()
                )
            )

        }
    }

    data class State(
        val list: List<CollectionModelStable> = emptyList(),
        val isLoading: Boolean = true,
        val isError: Boolean = false
    )
}

internal interface CollectionsComponentInternal: CollectionsComponent {
    fun refresh()
}