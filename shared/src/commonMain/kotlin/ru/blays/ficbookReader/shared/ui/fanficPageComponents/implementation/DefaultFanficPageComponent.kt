package ru.blays.ficbookReader.shared.ui.fanficPageComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
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

    private fun childFactory(configuration: FanficPageComponent.Config, childContext: ComponentContext): FanficPageComponent.Child {
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
                    output = ::onReaderOutput
                )
            )
            is FanficPageComponent.Config.Comments -> FanficPageComponent.Child.Comments(
                TODO()
            )
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
                        index = output.index,
                        chapters = output.chapters
                    )
                )
            }
            is FanficPageInfoComponent.Output.OpenComments -> {
                navigation.push(
                    FanficPageComponent.Config.Comments(output.href)
                )
            }
            is FanficPageInfoComponent.Output.OpenLastOrFirstChapter -> {
                val chapters = output.chapters
                val lastReadedChapterIndex = chapters.indexOfLast { it.readed }
                if(lastReadedChapterIndex != -1) {
                    navigation.push(
                        FanficPageComponent.Config.Reader(
                            index = lastReadedChapterIndex,
                            chapters = chapters
                        )
                    )
                } else if (chapters.isNotEmpty()) {
                    navigation.push(
                        FanficPageComponent.Config.Reader(
                            index = 1,
                            chapters = chapters
                        )
                    )
                }
            }
        }
    }

    private fun onReaderOutput(output: MainReaderComponent.Output) {
        when(output) {
            is MainReaderComponent.Output.NavigateBack -> navigation.pop()
        }
    }
}