package ru.blays.ficbookReader.shared.ui.fanficPageComponents.declaration

import com.arkivanov.decompose.value.Value
import ru.blays.ficbookReader.shared.data.dto.FanficChapterStable
import ru.blays.ficbookReader.shared.data.dto.FanficPageModelStable
import ru.blays.ficbookReader.shared.data.dto.SectionWithQuery

interface FanficPageInfoComponent {
    val state: Value<State>

    val actionsComponent: FanficPageActionsComponent

    fun sendIntent(intent: Intent)
    fun onOutput(output: Output)

    sealed class Intent {
        data object Refresh: Intent()
    }

    sealed class Output {
        data object ClosePage: Output()
        data object NavigateBack: Output()
        data class OpenChapter(val index: Int, val chapters: List<FanficChapterStable>): Output()
        data class OpenLastOrFirstChapter(val chapters: List<FanficChapterStable>): Output()
        data class OpenComments(val href: String): Output()
        data class OpenSection(val section: SectionWithQuery): Output() {
            constructor(
                name: String,
                href: String
            ): this(
                section = SectionWithQuery(
                    name = name,
                    href = href
                )
            )
        }
        data class OpenUrl(val url: String): Output()
    }

    data class State(
        val fanfic: FanficPageModelStable? = null,
        val isLoading: Boolean = true,
        val isError: Boolean = false
    )
}