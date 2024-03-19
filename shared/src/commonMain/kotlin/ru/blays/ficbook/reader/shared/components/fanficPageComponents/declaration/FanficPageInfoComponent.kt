package ru.blays.ficbook.reader.shared.components.fanficPageComponents.declaration

import com.arkivanov.decompose.value.Value
import ru.blays.ficbook.reader.shared.data.dto.FanficChapterStable
import ru.blays.ficbook.reader.shared.data.dto.FanficPageModelStable
import ru.blays.ficbook.reader.shared.data.dto.SectionWithQuery

interface FanficPageInfoComponent {
    val state: Value<State>

    val fanficHref: String

    val actionsComponent: FanficPageActionsComponent

    fun sendIntent(intent: Intent)
    fun onOutput(output: Output)

    sealed class Intent {
        data object Refresh: Intent()
        data object Share: Intent()
        data object CopyLink: Intent()
        data object OpenInBrowser: Intent()

        data class ChangeChaptersOrder(val reverse: Boolean): Intent()
    }

    sealed class Output {
        data object ClosePage: Output()
        data object NavigateBack: Output()
        data class OpenChapter(
            val fanficID: String,
            val index: Int,
            val chapters: FanficChapterStable
        ): Output()
        data class OpenLastOrFirstChapter(
            val fanficID: String,
            val chapter: FanficChapterStable
        ): Output()
        data class OpenPartComments(val chapterID: String): Output()
        data class OpenAllComments(val href: String): Output()
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
        data class OpenAuthor(val href: String) : Output()
        data class DownloadFanfic(val fanficID: String, val fanficName: String): Output()
        data class OpenAssociatedCollections(val fanficID: String): Output()
    }

    data class State(
        val fanfic: FanficPageModelStable? = null,
        val reverseOrderEnabled: Boolean,
        val isLoading: Boolean = true,
        val isError: Boolean = false
    )
}