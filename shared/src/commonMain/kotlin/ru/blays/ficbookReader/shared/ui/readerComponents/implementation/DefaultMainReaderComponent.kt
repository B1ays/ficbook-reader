package ru.blays.ficbookReader.shared.ui.readerComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbookReader.shared.data.dto.FanficChapterStable
import ru.blays.ficbookReader.shared.data.repo.declaration.IChaptersRepo
import ru.blays.ficbookReader.shared.preferences.SettingsKeys
import ru.blays.ficbookReader.shared.preferences.repositiry.ISettingsJsonRepository
import ru.blays.ficbookReader.shared.ui.readerComponents.declaration.MainReaderComponent
import ru.blays.ficbookapi.result.ApiResult

class DefaultMainReaderComponent(
    componentContext: ComponentContext,
    private val chapters: FanficChapterStable,
    initialChapterIndex: Int,
    private val fanficID: String,
    private val output: (output: MainReaderComponent.Output) -> Unit
) : MainReaderComponent, ComponentContext by componentContext {
    private val chaptersRepository: IChaptersRepo by getKoin().inject()
    private val settingsJsonRepository: ISettingsJsonRepository by getKoin().inject()

    private var readerSettings by settingsJsonRepository.getDelegate(
        serializer = MainReaderComponent.Settings.serializer(),
        defaultValue = MainReaderComponent.Settings(),
        key = SettingsKeys.READER_PREFS_KEY
    )

    private val _state = MutableValue(
        createState(
            chapters = chapters,
            initialChapterIndex = initialChapterIndex
        )
    )
    override val state get() = _state

    private val dialogNavigation = SlotNavigation<MainReaderComponent.SettingsDialogConfig>()

    override val dialog = childSlot(
        source = dialogNavigation,
        serializer = MainReaderComponent.SettingsDialogConfig.serializer(),
        handleBackButton = true
    ) { config, childComponentContext ->
        DefaultSettingsReaderComponent(
            componentContext = childComponentContext,
            config.settings,
            onSettingsChanged = ::onSettingsChanged
        )
    }
    override val voteComponent = DefaultVoteReaderComponent(
        componentContext = childContext("voteComponent"),
        chapters = chapters,
        fanficID = fanficID
    )

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        openChapter(initialChapterIndex)
    }

    override fun sendIntent(intent: MainReaderComponent.Intent) {
        when (intent) {
            is MainReaderComponent.Intent.ChangeChapter -> {
                openChapter(intent.chapterIndex)
            }
            is MainReaderComponent.Intent.OpenOrCloseSettings -> {
                if (dialog.value.child == null) {
                    dialogNavigation.activate(
                        MainReaderComponent.SettingsDialogConfig(state.value.settings)
                    )
                } else {
                    dialogNavigation.dismiss()
                }
            }
            is MainReaderComponent.Intent.SaveProgress -> {
                saveReadProgress(
                    chapter = chapters,
                    fanficID = fanficID,
                    chapterIndex = intent.chapterIndex,
                    charIndex = intent.charIndex
                )
            }
        }
    }

    override fun onOutput(output: MainReaderComponent.Output) {
        this.output(output)
    }

    private fun onSettingsChanged(settings: MainReaderComponent.Settings) {
        _state.update {
            it.copy(
                settings = settings
            )
        }
        readerSettings = settings
    }

    private fun openChapter(index: Int) = coroutineScope.launch {
        if(chapters is FanficChapterStable.SeparateChaptersModel && index in chapters.chapters.indices) {
            _state.update {
                it.copy(loading = true)
            }
            val chapter = chapters.chapters[index]
            when(
                val textResult = chaptersRepository.getChapterText(chapter.href)
            ) {
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            loading = false,
                            error = true
                        )
                    }
                }
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            chapterIndex = index,
                            initialCharIndex = chapter.lastWatchedCharIndex,
                            chapterName = chapter.name,
                            text = textResult.value,
                            loading = false,
                            error = false
                        )
                    }
                }
            }
        }
    }

    private fun createState(
        chapters: FanficChapterStable,
        initialChapterIndex: Int,
    ): MainReaderComponent.State {
        return when(chapters) {
            is FanficChapterStable.SeparateChaptersModel -> {
                val chapter = chapters.chapters[initialChapterIndex]
                MainReaderComponent.State(
                    chapterIndex = initialChapterIndex,
                    chaptersCount = chapters.chaptersCount,
                    initialCharIndex = chapter.lastWatchedCharIndex,
                    chapterName = chapter.name,
                    text = "",
                    loading = false,
                    error = false,
                    settings = readerSettings
                )
            }
            is FanficChapterStable.SingleChapterModel -> {
                MainReaderComponent.State(
                    chapterIndex = 0,
                    initialCharIndex = 0,
                    chapterName = "Глава 1",
                    text = chapters.text,
                    loading = false,
                    error = false,
                    settings = readerSettings
                )
            }
        }
    }

    private fun saveReadProgress(
        chapter: FanficChapterStable,
        fanficID: String,
        chapterIndex: Int,
        charIndex: Int
    ) = coroutineScope.launch {
        if(chapter is FanficChapterStable.SeparateChaptersModel) {
            chaptersRepository.saveReadProgress(
                chapter = chapter.chapters[chapterIndex],
                fanficID = fanficID,
                charIndex = charIndex
            )
        }
    }
}
