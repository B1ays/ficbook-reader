package ru.blays.ficbookReader.shared.ui.fanficPageComponents.declaration

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import ru.blays.ficbookReader.shared.data.dto.FanficChapterStable
import ru.blays.ficbookReader.shared.data.dto.SectionWithQuery
import ru.blays.ficbookReader.shared.ui.commentsComponent.CommentsComponent
import ru.blays.ficbookReader.shared.ui.readerComponents.declaration.MainReaderComponent

interface FanficPageComponent {
    val childStack: Value<ChildStack<*, Child>>

    sealed class Output {
        data object NavigateBack: Output()
        data class OpenUrl(val url: String): Output()
        data class OpenSection(val section: SectionWithQuery) : Output()
        data class OpenAuthor(val href: String) : Output()
        class OpenAnotherFanfic(val href: String) : Output()
    }

    sealed class Child {
        data class Info(val component: FanficPageInfoComponent): Child()
        data class Reader(val component: MainReaderComponent): Child()
        data class PartComments(val component: CommentsComponent): Child()
        data class AllComments(val component: CommentsComponent): Child()
    }

    @Serializable
    sealed class Config {
        @Serializable
        data class Info(val href: String): Config()
        @Serializable
        data class Reader(
            val fanficID: String,
            val index: Int,
            val chapters: List<FanficChapterStable>
        ): Config()
        @Serializable
        data class PartComments(val href: String): Config()
        @Serializable
        data class AllComments(val href: String): Config()
    }
}