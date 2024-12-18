package ru.blays.ficbook.reader.shared.components.fanficPageComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import ru.blays.ficbook.api.data.SectionWithQuery
import ru.blays.ficbook.reader.shared.components.collectionComponents.declaration.CollectionsListComponent
import ru.blays.ficbook.reader.shared.components.collectionComponents.implementation.DefaultCollectionsListComponent
import ru.blays.ficbook.reader.shared.components.commentsComponent.declaration.CommentsComponent
import ru.blays.ficbook.reader.shared.components.commentsComponent.implementation.DefaultAllCommentsComponent
import ru.blays.ficbook.reader.shared.components.commentsComponent.implementation.DefaultPartCommentsComponent
import ru.blays.ficbook.reader.shared.components.fanficPageComponents.declaration.FanficPageComponent
import ru.blays.ficbook.reader.shared.components.fanficPageComponents.declaration.FanficPageInfoComponent
import ru.blays.ficbook.reader.shared.components.readerComponents.declaration.MainReaderComponent
import ru.blays.ficbook.reader.shared.components.readerComponents.implementation.DefaultMainReaderComponent
import ru.blays.ficbook.reader.shared.data.dto.FanficChapterStable

class DefaultFanficPageComponent(
    componentContext: ComponentContext,
    private val fanficHref: String,
    private val onOutput: (FanficPageComponent.Output) -> Unit
): FanficPageComponent, ComponentContext by componentContext {
    private val navigation = StackNavigation<FanficPageComponent.Config>()

    override val childStack = childStack(
        source = navigation,
        initialConfiguration = FanficPageComponent.Config.Info(fanficHref),
        serializer = FanficPageComponent.Config.serializer(),
        childFactory = ::childFactory,
        handleBackButton = true
    )

    private fun childFactory(
        configuration: FanficPageComponent.Config,
        childContext: ComponentContext
    ): FanficPageComponent.Child {
        return when(configuration) {
            is FanficPageComponent.Config.Info -> FanficPageComponent.Child.Info(
                DefaultFanficPageInfoComponent(
                    componentContext = childContext,
                    fanficHref = fanficHref,
                    onOutput = ::onInfoOutput
                )
            )
            is FanficPageComponent.Config.Reader -> FanficPageComponent.Child.Reader(
                DefaultMainReaderComponent(
                    componentContext = childContext,
                    chapters = configuration.chapter,
                    initialChapterIndex = configuration.index,
                    fanficID = configuration.fanficID,
                    onOutput = ::onReaderOutput
                )
            )
            is FanficPageComponent.Config.PartComments -> FanficPageComponent.Child.PartComments(
                DefaultPartCommentsComponent(
                    componentContext = childContext,
                    partID = configuration.chapterID,
                    output = ::onCommentsOutput
                )
            )
            is FanficPageComponent.Config.AllComments -> {
                FanficPageComponent.Child.AllComments(
                    DefaultAllCommentsComponent(
                        componentContext = childContext,
                        href = configuration.href,
                        output = ::onCommentsOutput
                    )
                )
            }
            is FanficPageComponent.Config.DownloadFanfic -> {
                FanficPageComponent.Child.DownloadFanfic(
                    DefaultDownloadFanficComponent(
                        componentContext = childContext,
                        fanficID = configuration.fanficID,
                        fanficName = configuration.fanficName,
                        onClose = navigation::pop
                    )
                )
            }
            is FanficPageComponent.Config.AssociatedCollections -> {
                FanficPageComponent.Child.AssociatedCollections(
                    DefaultCollectionsListComponent(
                        componentContext = childContext,
                        section = SectionWithQuery(
                            href = "collections/${configuration.fanficID}/list"
                        ),
                        onOutput = ::onCollectionsOutput
                    ).apply { refresh() }
                )
            }
        }
    }

    private fun onInfoOutput(output: FanficPageInfoComponent.Output) {
        when(output) {
            is FanficPageInfoComponent.Output.ClosePage -> {
                onOutput(FanficPageComponent.Output.NavigateBack)
            }
            is FanficPageInfoComponent.Output.NavigateBack -> {
                navigation.pop()
            }
            is FanficPageInfoComponent.Output.OpenChapter -> {
                navigation.pushNew(
                    FanficPageComponent.Config.Reader(
                        fanficID = output.fanficID,
                        index = output.index,
                        chapter = output.chapters
                    )
                )
            }
            is FanficPageInfoComponent.Output.OpenPartComments -> {
                navigation.pushNew(
                    FanficPageComponent.Config.PartComments(
                        chapterID = output.chapterID
                    )
                )
            }
            is FanficPageInfoComponent.Output.OpenAllComments -> {
                navigation.pushNew(
                    FanficPageComponent.Config.AllComments(output.href)
                )
            }
            is FanficPageInfoComponent.Output.OpenLastOrFirstChapter -> {
               openLastReadedChapter(
                   chapters = output.chapter,
                   fanficID = output.fanficID
               )
            }
            is FanficPageInfoComponent.Output.OpenUrl -> {
                onOutput(
                    FanficPageComponent.Output.OpenUrl(output.url)
                )
            }

            is FanficPageInfoComponent.Output.OpenSection -> {
                onOutput(
                    FanficPageComponent.Output.OpenSection(output.section)
                )
            }
            is FanficPageInfoComponent.Output.OpenAuthor -> {
                onOutput(
                    FanficPageComponent.Output.OpenAuthor(output.href)
                )
            }
            is FanficPageInfoComponent.Output.DownloadFanfic -> {
                navigation.pushNew(
                    FanficPageComponent.Config.DownloadFanfic(
                        fanficID = output.fanficID,
                        fanficName = output.fanficName,
                    )
                )
            }
            is FanficPageInfoComponent.Output.OpenAssociatedCollections -> {
                navigation.pushNew(
                    FanficPageComponent.Config.AssociatedCollections(
                        fanficID = output.fanficID
                    )
                )
            }
        }
    }

    private fun onCommentsOutput(output: CommentsComponent.Output) {
        when(output) {
            is CommentsComponent.Output.NavigateBack -> navigation.pop()
            is CommentsComponent.Output.OpenAuthor -> {
                onOutput(
                    FanficPageComponent.Output.OpenAuthor(output.href)
                )
            }
            is CommentsComponent.Output.OpenUrl -> {
                onOutput(
                    FanficPageComponent.Output.OpenUrl(output.url)
                )
            }
            is CommentsComponent.Output.OpenFanfic -> {
                onOutput(
                    FanficPageComponent.Output.OpenAnotherFanfic(output.href)
                )
            }
        }
    }

    private fun openLastReadedChapter(
        chapters: FanficChapterStable,
        fanficID: String
    ) {
        when(chapters) {
            is FanficChapterStable.SeparateChaptersModel -> {
                val index = chapters.chapters.indexOfLast { it.readed }
                navigation.pushNew(
                    FanficPageComponent.Config.Reader(
                        fanficID = fanficID,
                        index = if(index == -1) 0 else index,
                        chapter = chapters
                    )
                )
            }
            is FanficChapterStable.SingleChapterModel -> {
                navigation.pushNew(
                    FanficPageComponent.Config.Reader(
                        fanficID = fanficID,
                        index = 0,
                        chapter = chapters
                    )
                )
            }
        }
    }

    private fun onReaderOutput(output: MainReaderComponent.Output) {
        when(output) {
            is MainReaderComponent.Output.Close -> {
                navigation.pop { success ->
                    if(success) {
                        val activeChild = childStack.active.instance
                        if(activeChild is FanficPageComponent.Child.Info) {
                            activeChild.component.sendIntent(
                                FanficPageInfoComponent.Intent.UpdateChapters(output.chapters)
                            )
                        }
                    }
                }
            }
            MainReaderComponent.Output.NavigateBack -> navigation.pop()
        }
    }

    private fun onCollectionsOutput(output: CollectionsListComponent.Output) {
        when(output) {
            is CollectionsListComponent.Output.OpenCollection -> {
                onOutput(
                    FanficPageComponent.Output.OpenCollection(
                        relativeID = output.relativeID,
                        realID = output.realID
                    )
                )
            }
            is CollectionsListComponent.Output.OpenUser -> {
                onOutput(
                    FanficPageComponent.Output.OpenAuthor(output.owner.href)
                )
            }
            CollectionsListComponent.Output.NavigateBack -> navigation.pop()
        }
    }
}