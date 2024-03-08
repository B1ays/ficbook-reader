package ru.blays.ficbook.reader.shared.components.fanficPageComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import ru.blays.ficbook.reader.shared.data.dto.FanficChapterStable
import ru.blays.ficbook.reader.shared.components.commentsComponent.declaration.CommentsComponent
import ru.blays.ficbook.reader.shared.components.commentsComponent.implementation.DefaultAllCommentsComponent
import ru.blays.ficbook.reader.shared.components.commentsComponent.implementation.DefaultPartCommentsComponent
import ru.blays.ficbook.reader.shared.components.fanficPageComponents.declaration.FanficPageComponent
import ru.blays.ficbook.reader.shared.components.fanficPageComponents.declaration.FanficPageInfoComponent
import ru.blays.ficbook.reader.shared.components.readerComponents.declaration.MainReaderComponent
import ru.blays.ficbook.reader.shared.components.readerComponents.implementation.DefaultMainReaderComponent

class DefaultFanficPageComponent(
    componentContext: ComponentContext,
    private val fanficHref: String,
    private val onOutput: (FanficPageComponent.Output) -> Unit
): FanficPageComponent, ComponentContext by componentContext {
    private val navigation = StackNavigation<FanficPageComponent.Config>()
    override val childStack: Value<ChildStack<*, FanficPageComponent.Child>> = childStack(
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
                    output = ::onReaderOutput
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
                        close = navigation::pop
                    )
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
                navigation.push(
                    FanficPageComponent.Config.Reader(
                        fanficID = output.fanficID,
                        index = output.index,
                        chapter = output.chapters
                    )
                )
            }
            is FanficPageInfoComponent.Output.OpenPartComments -> {
                navigation.push(
                    FanficPageComponent.Config.PartComments(
                        chapterID = output.chapterID
                    )
                )
            }
            is FanficPageInfoComponent.Output.OpenAllComments -> {
                navigation.push(
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
                navigation.push(
                    FanficPageComponent.Config.DownloadFanfic(
                        fanficID = output.fanficID,
                        fanficName = output.fanficName
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
        when(
            chapters
        ) {
            is FanficChapterStable.SeparateChaptersModel -> {
                val index = chapters.chapters.indexOfLast { it.readed }
                navigation.push(
                    FanficPageComponent.Config.Reader(
                        fanficID = fanficID,
                        index = if(index == -1) 0 else index,
                        chapter = chapters
                    )
                )
            }
            is FanficChapterStable.SingleChapterModel -> {
                navigation.push(
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
            is MainReaderComponent.Output.NavigateBack -> navigation.pop()
        }
    }
}