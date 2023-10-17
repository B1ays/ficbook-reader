package ru.blays.ficbookReader.shared.ui.readerComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.*
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.blays.ficbookReader.shared.data.dto.FanficChapterStable
import ru.blays.ficbookReader.shared.ui.readerComponents.declaration.MainReaderComponent
import ru.blays.ficbookReader.shared.ui.readerComponents.declaration.SettingsReaderComponent
import ru.blays.ficbookapi.ficbookConnection.IFicbookApi

class DefaultMainReaderComponent(
    componentContext: ComponentContext,
    private val ficbookApi: IFicbookApi,
    private val chapters: List<FanficChapterStable>,
    initialChapterIndex: Int,
    private val output: (output: MainReaderComponent.Output) -> Unit
): MainReaderComponent, ComponentContext by componentContext {
    private val _state: MutableValue<MainReaderComponent.State> = MutableValue(
        MainReaderComponent.State(
            chapterIndex = initialChapterIndex,
            chaptersCount = chapters.size,
            chapterName = chapters[initialChapterIndex].run {
                if(this is FanficChapterStable.SeparateChapterModel) name else "Глава 1"
            },
            text = "",
            loading = true,
            error = false,
            settings = getSettings()
        )
    )
    override val state: Value<MainReaderComponent.State>
        get() = _state

    private val dialogNavigation = SlotNavigation<MainReaderComponent.DialogConfig>()
    override val dialog: Value<ChildSlot<*, SettingsReaderComponent>> = childSlot(
        serializer = MainReaderComponent.DialogConfig.serializer(),
        source = dialogNavigation,
        handleBackButton = true
    ) { config, childComponentContext ->
        DefaultSettingsReaderComponent(
            componentContext = childComponentContext,
            initialSettings = config.settings,
            onSettingsChanged = ::onSettingsChanged
        )
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        openChapter(initialChapterIndex)
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }

    override fun onIntent(intent: MainReaderComponent.Intent) {
        when(intent) {
            is MainReaderComponent.Intent.ChangeChapter -> {
                openChapter(intent.chapterIndex)
            }
            is MainReaderComponent.Intent.OpenCloseSettings -> {
                if(dialog.value.child == null) {
                    dialogNavigation.activate(
                        MainReaderComponent.DialogConfig(state.value.settings)
                    )
                } else {
                    dialogNavigation.dismiss()
                }
            }
        }
    }

    override fun onOutput(output: MainReaderComponent.Output) {
        this.output.invoke(output)
    }

    private fun onSettingsChanged(settings: MainReaderComponent.Settings) {
        _state.update {
            it.copy(
                settings = settings
            )
        }
    }

    private fun openChapter(index: Int) {
        if(index in chapters.indices) {
            when(val chapter = chapters[index]) {
                is FanficChapterStable.SeparateChapterModel -> {
                    _state.update {
                        it.copy(
                            loading = true,
                            text = ""
                        )
                    }
                    coroutineScope.launch {
                        val text = ficbookApi.getFanficChapterText(chapter.href)
                        if(text != null) {
                            _state.update {
                                it.copy(
                                    text = text,
                                    loading = false,
                                    chapterIndex = index,
                                    chapterName = chapter.name,
                                )
                            }
                        } else {
                            _state.update {
                                it.copy(
                                    text = "Ошибка загрузки главы",
                                    loading = false,

                                )
                            }
                        }
                    }
                }
                is FanficChapterStable.SingleChapterModel -> {
                    _state.update {
                        it.copy(
                            text = chapter.text
                        )
                    }
                }
            }
        }
    }

    private fun getSettings(): MainReaderComponent.Settings {
        return MainReaderComponent.Settings()
    }
}