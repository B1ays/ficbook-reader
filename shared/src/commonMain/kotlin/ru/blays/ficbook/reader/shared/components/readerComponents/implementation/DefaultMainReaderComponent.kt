package ru.blays.ficbook.reader.shared.components.readerComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnStart
import com.russhwolf.settings.boolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.components.readerComponents.declaration.MainReaderComponent
import ru.blays.ficbook.reader.shared.data.dto.FanficChapterStable
import ru.blays.ficbook.reader.shared.data.repo.declaration.IChaptersRepo
import ru.blays.ficbook.reader.shared.preferences.SettingsKeys
import ru.blays.ficbook.reader.shared.preferences.repositiry.ISettingsJsonRepository
import ru.blays.ficbook.reader.shared.preferences.settings
import ru.blays.ficbook.reader.shared.stateHandle.SaveableMutableValue

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

    private val typografEnabled by settings.boolean(
        key = SettingsKeys.TYPOGRAF_KEY,
        defaultValue = true
    )

    private val _state = SaveableMutableValue(
        serializer = MainReaderComponent.State.serializer(),
        initialValue = createState(
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
        fanficID = fanficID,
        lastChapter = {
            when(chapters) {
                is FanficChapterStable.SeparateChaptersModel -> {
                    chapters.chapters.lastIndex == state.value.chapterIndex
                }
                else -> false
            }
        }
    )

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

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
                    val text = if(typografEnabled) {
                        typograf(textResult.value)
                    } else {
                        textResult.value
                    }
                    _state.update {
                        it.copy(
                            chapterIndex = index,
                            initialCharIndex = chapter.lastWatchedCharIndex,
                            chapterName = chapter.name,
                            text = text,
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
                    text = if(typografEnabled) {
                        typograf(chapters.text)
                    } else {
                        chapters.text
                    },
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

    private fun typograf(input: String): String = try {
        val lines = input.lines()

        lines.fold(StringBuilder()) { stringBuilder, line ->
            var clearedLine = line.trim()

            if (clearedLine.contains("\"")) {
                clearedLine = clearedLine.replace("\"([\\w\\s—.:,!?\\-]+)\"".toRegex(), "«$1»")
            }

            if (clearedLine.contains('-')) {
                clearedLine = clearedLine.replace("--", "-")
                clearedLine = clearedLine.replace(",-", ", —")
                clearedLine = clearedLine.replace(", -", ", —")
                clearedLine = clearedLine.replace(".-", ". —")
                clearedLine = clearedLine.replace(". -", ". —")
                clearedLine = clearedLine.replace("!-", "! —")
                clearedLine = clearedLine.replace("! -", "! —")
                clearedLine = clearedLine.replace("?-", "? —")
                clearedLine = clearedLine.replace("? -", "? —")
            }

            if (clearedLine.startsWith("-")) {
                clearedLine = "—" + clearedLine.substring(1)
            }
            if (clearedLine.startsWith("—")) {
                if (!clearedLine.startsWith("— ")) {
                    clearedLine = "— " + clearedLine.substring(1)
                }
            }
            stringBuilder.appendLine("$INDENT_SPACES$clearedLine")
        }.toString()
    } catch (e: Exception) {
        e.printStackTrace()
        input
    }

    init {
        lifecycle.doOnStart(true) {
            val state = state.value
            if(state.text.isEmpty() && !state.error) {
                openChapter(initialChapterIndex)
            }
        }
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }

    companion object {
        private const val INDENT_SPACES = "\u00A0\u00A0\u00A0\u00A0"
    }
}
