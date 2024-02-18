package ru.blays.ficbookReader.shared.ui.fanficListComponents.declaration

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import ru.blays.ficbookReader.shared.data.dto.FanficCardModelStable
import ru.blays.ficbookReader.shared.data.dto.SectionWithQuery
import ru.blays.ficbookReader.shared.ui.fanficListComponents.implementation.FanficsListDialogComponent

interface FanficsListComponent {

    val state: Value<State>

    val dialog: Value<ChildSlot<*, FanficsListDialogComponent>>

    fun sendIntent(intent: Intent)
    fun onOutput(output: Output)

    fun getQuickActionsComponent(fanficID: String): FanficQuickActionsComponent

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

    data class State(
        val section: SectionWithQuery,
        val list: List<FanficCardModelStable>,
        val isLoading: Boolean
    )

    @Serializable
    data class DialogConfig(val message: String)
}

internal interface FanficsListComponentInternal: FanficsListComponent {
    fun setSection(section: ru.blays.ficbookapi.data.SectionWithQuery)
}