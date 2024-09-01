package ru.blays.ficbook.reader.shared.components.fanficListComponents.declaration

import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import ru.blays.ficbook.reader.shared.data.dto.FanficCardModelStable
import ru.blays.ficbook.reader.shared.data.dto.SectionWithQuery

interface FanficsListComponent {

    val state: Value<State>

    fun sendIntent(intent: Intent)
    fun onOutput(output: Output)

    fun getQuickActionsComponent(fanficID: String, fanficName: String): FanficQuickActionsComponent

    sealed class Intent {
        data object Refresh: Intent()
        data object LoadNextPage: Intent()
        data class SetAsFeed(val section: SectionWithQuery): Intent()
    }

    sealed class Output {
        data object NavigateBack: Output()
        data class OpenFanfic(val href: String): Output()
        data class OpenAnotherSection(val section: SectionWithQuery): Output()
        data class OpenUrl(val url: String): Output()
        data class OpenAuthor(val href: String) : Output()
    }

    @Serializable
    data class State(
        val section: SectionWithQuery,
        val list: List<FanficCardModelStable>,
        val isLoading: Boolean
    )

    @Serializable
    data class DialogConfig(val message: String)
}

internal interface FanficsListComponentInternal: FanficsListComponent {
    fun setSection(section: ru.blays.ficbook.api.data.SectionWithQuery)
}