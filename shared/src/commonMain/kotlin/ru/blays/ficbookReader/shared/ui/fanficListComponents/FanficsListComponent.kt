package ru.blays.ficbookReader.shared.ui.fanficListComponents

import com.arkivanov.decompose.value.Value
import ru.blays.ficbookReader.shared.data.dto.FanficCardModelStable
import ru.blays.ficbookReader.shared.data.dto.SectionWithQuery

interface FanficsListComponent {

    val state: Value<State>

    fun sendIntent(intent: Intent)
    fun onOutput(output: Output)

    sealed class Intent {
        data object Refresh: Intent()
        data object LoadNextPage: Intent()
        data class SetAsFeed(val section: SectionWithQuery): Intent()
    }

    sealed class Output {
        data object NavigateBack: Output()
        data class OpenFanfic(val href: String): Output()
    }

    data class State(
        val section: SectionWithQuery,
        val list: List<FanficCardModelStable> = emptyList(),
        val isLoading: Boolean = true,
        val page: Int = 1
    )
}

internal interface FanficsListComponentInternal: FanficsListComponent {
    fun setSection(section: ru.blays.ficbookapi.data.SectionWithQuery)
}