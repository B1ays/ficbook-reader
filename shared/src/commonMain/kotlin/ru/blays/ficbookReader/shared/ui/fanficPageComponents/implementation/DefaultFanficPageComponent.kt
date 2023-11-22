package ru.blays.ficbookReader.shared.ui.fanficPageComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import ru.blays.ficbookReader.shared.ui.commentsComponent.CommentsComponent
import ru.blays.ficbookReader.shared.ui.commentsComponent.DefaultAllCommentsComponent
import ru.blays.ficbookReader.shared.ui.commentsComponent.DefaultPartCommentsComponent
import ru.blays.ficbookReader.shared.ui.fanficPageComponents.declaration.FanficPageComponent
import ru.blays.ficbookReader.shared.ui.fanficPageComponents.declaration.FanficPageInfoComponent
import ru.blays.ficbookReader.shared.ui.readerComponents.declaration.MainReaderComponent
import ru.blays.ficbookReader.shared.ui.readerComponents.implementation.DefaultMainReaderComponent
import ru.blays.ficbookapi.ficbookConnection.IFicbookApi

class DefaultFanficPageComponent(
    componentContext: ComponentContext,
    private val ficbookApi: IFicbookApi,
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
                    ficbookApi = ficbookApi,
                    fanficHref = fanficHref,
                    onOutput = ::onInfoOutput
                )
            )
            is FanficPageComponent.Config.Reader -> FanficPageComponent.Child.Reader(
                DefaultMainReaderComponent(
                    componentContext = childContext,
                    ficbookApi = ficbookApi,
                    chapters = configuration.chapters,
                    initialChapterIndex = configuration.index,
                    fanficID = configuration.fanficID,
                    output = ::onReaderOutput
                )
            )
            is FanficPageComponent.Config.PartComments -> FanficPageComponent.Child.PartComments(
                DefaultPartCommentsComponent.createWithHref(
                    componentContext = childContext,
                    ficbookApi = ficbookApi,
                    href = configuration.href,
                    output = ::onCommentsOutput
                )
            )
            is FanficPageComponent.Config.AllComments -> {
                FanficPageComponent.Child.AllComments(
                    DefaultAllCommentsComponent(
                        componentContext = childContext,
                        ficbookApi = ficbookApi,
                        href = configuration.href,
                        output = ::onCommentsOutput
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
                        chapters = output.chapters
                    )
                )
            }
            is FanficPageInfoComponent.Output.OpenPartComments -> {
                navigation.push(
                    FanficPageComponent.Config.PartComments(output.href)
                )
            }
            is FanficPageInfoComponent.Output.OpenAllComments -> {
                navigation.push(
                    FanficPageComponent.Config.AllComments(output.href)
                )
            }
            is FanficPageInfoComponent.Output.OpenLastOrFirstChapter -> {
                val chapters = output.chapters
                val lastReadedChapterIndex = chapters.indexOfLast { it.readed }
                if(lastReadedChapterIndex != -1) {
                    navigation.push(
                        FanficPageComponent.Config.Reader(
                            fanficID = output.fanficID,
                            index = lastReadedChapterIndex,
                            chapters = chapters
                        )
                    )
                } else if (chapters.isNotEmpty()) {
                    navigation.push(
                        FanficPageComponent.Config.Reader(
                            fanficID = output.fanficID,
                            index = 0,
                            chapters = chapters
                        )
                    )
                }
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

    private fun onReaderOutput(output: MainReaderComponent.Output) {
        when(output) {
            is MainReaderComponent.Output.NavigateBack -> navigation.pop()
        }
    }

    private fun getIDFromHref(href: String): String {
        val clearedHref = href
            .substringBefore('?')
            .removeSuffix("/")

        return clearedHref.substringAfterLast('/').substringBefore('#')
    }
}