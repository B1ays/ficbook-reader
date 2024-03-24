package ru.blays.ficbook.reader.shared.components.fanficPageComponents.declaration

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import ru.blays.ficbook.reader.shared.components.collectionComponents.declaration.CollectionsListComponent
import ru.blays.ficbook.reader.shared.components.commentsComponent.declaration.CommentsComponent
import ru.blays.ficbook.reader.shared.components.commentsComponent.implementation.DefaultPartCommentsComponent
import ru.blays.ficbook.reader.shared.components.readerComponents.declaration.MainReaderComponent
import ru.blays.ficbook.reader.shared.data.dto.FanficChapterStable
import ru.blays.ficbook.reader.shared.data.dto.SectionWithQuery

interface FanficPageComponent {
    val childStack: Value<ChildStack<*, Child>>

    sealed class Output {
        data object NavigateBack: Output()
        data class OpenUrl(val url: String): Output()
        data class OpenSection(val section: SectionWithQuery) : Output()
        data class OpenAuthor(val href: String) : Output()
        data class OpenAnotherFanfic(val href: String) : Output()
        data class OpenCollection(val relativeID: String, val realID: String) : Output()
    }

    sealed class Child {
        data class Info(val component: FanficPageInfoComponent): Child()
        data class Reader(val component: MainReaderComponent): Child()
        data class PartComments(val component: DefaultPartCommentsComponent): Child()
        data class AllComments(val component: CommentsComponent): Child()
        data class DownloadFanfic(val component: DownloadFanficComponent): Child()
        data class AssociatedCollections(val component: CollectionsListComponent): Child()
    }

    @Serializable
    sealed class Config {
        @Serializable
        data class Info(val href: String): Config()
        @Serializable
        data class Reader(
            val fanficID: String,
            val index: Int,
            val chapter: FanficChapterStable
        ): Config()
        @Serializable
        data class PartComments(val chapterID: String): Config()
        @Serializable
        data class AllComments(val href: String): Config()
        @Serializable
        data class DownloadFanfic(
            val fanficID: String,
            val fanficName: String,
        ): Config()
        @Serializable
        data class AssociatedCollections(val fanficID: String): Config()
    }
}