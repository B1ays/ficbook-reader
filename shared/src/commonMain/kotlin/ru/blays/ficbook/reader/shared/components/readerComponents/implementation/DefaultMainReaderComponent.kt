package ru.blays.ficbook.reader.shared.components.readerComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnStart
import com.russhwolf.settings.boolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.blays.ficbook.api.result.ResponseResult
import ru.blays.ficbook.reader.shared.components.readerComponents.declaration.MainReaderComponent
import ru.blays.ficbook.reader.shared.data.dto.FanficChapterStable
import ru.blays.ficbook.reader.shared.data.repo.declaration.IChaptersRepo
import ru.blays.ficbook.reader.shared.preferences.SettingsKeys
import ru.blays.ficbook.reader.shared.preferences.repositiry.ISettingsJsonRepository
import ru.blays.ficbook.reader.shared.preferences.settings
import ru.blays.ficbook.reader.shared.stateHandle.SaveableMutableValue

class DefaultMainReaderComponent(
    componentContext: ComponentContext,
    private var chapters: FanficChapterStable,
    initialChapterIndex: Int,
    private val fanficID: String,
    private val onOutput: (output: MainReaderComponent.Output) -> Unit
) : MainReaderComponent, ComponentContext by componentContext, KoinComponent {
    private val chaptersRepository: IChaptersRepo by inject()
    private val settingsJsonRepository: ISettingsJsonRepository by inject()

    private val dialogNavigation = SlotNavigation<MainReaderComponent.SettingsDialogConfig>()

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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

    private val backCallback = BackCallback {
        onOutput(MainReaderComponent.Output.Close(chapters))
    }

    override val state
        get() = _state

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
            when(val currentChapters = chapters) {
                is FanficChapterStable.SeparateChaptersModel -> {
                    currentChapters.chapters.lastIndex == state.value.chapterIndex
                }
                else -> false
            }
        }
    )

    override fun sendIntent(intent: MainReaderComponent.Intent) {
        when (intent) {
            is MainReaderComponent.Intent.ChangeChapter -> coroutineScope.launch {
                openChapter(intent.chapterIndex)
            }
            is MainReaderComponent.Intent.ChangeDialogVisible -> {
                if (dialog.value.child == null) {
                    dialogNavigation.activate(
                        MainReaderComponent.SettingsDialogConfig(state.value.settings)
                    )
                } else {
                    dialogNavigation.dismiss()
                }
            }
            is MainReaderComponent.Intent.SaveProgress -> coroutineScope.launch {
                saveReadProgress(
                    chapterIndex = intent.chapterIndex,
                    charIndex = intent.charIndex
                )
            }
        }
    }

    override fun onOutput(output: MainReaderComponent.Output) {
        when(output) {
            is MainReaderComponent.Output.Close -> onOutput.invoke(output)
            MainReaderComponent.Output.NavigateBack -> onOutput.invoke(
                MainReaderComponent.Output.Close(chapters)
            )
        }
    }

    private fun onSettingsChanged(settings: MainReaderComponent.Settings) {
        _state.update {
            it.copy(
                settings = settings
            )
        }
        readerSettings = settings
    }

    private suspend fun openChapter(index: Int) {
        val chapters = chapters
        if(chapters is FanficChapterStable.SeparateChaptersModel) {
            _state.update {
                it.copy(loading = true)
            }
            val chapter = chapters.chapters[index]
            when(
                val chapterResult = chaptersRepository.getChapterHtml(chapter.href)
            ) {
                is ResponseResult.Error -> {
                    _state.update {
                        it.copy(
                            loading = false,
                            error = true
                        )
                    }
                }
                is ResponseResult.Success -> {
                    val text = if(typografEnabled) {
                        typograf(chapterResult.value)
                    } else {
                        chapterResult.value
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
                    chapterName = "",
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

    private suspend fun saveReadProgress(
        chapterIndex: Int,
        charIndex: Int
    ) {
        val chapters = chapters
        if(chapters is FanficChapterStable.SeparateChaptersModel) {
            val success = chaptersRepository.saveReadProgress(
                chapter = chapters.chapters[chapterIndex],
                fanficID = fanficID,
                charIndex = charIndex
            )
            if(success) {
                val newChapters = chapters.chapters.toMutableList().apply {
                    val oldChapter = this[chapterIndex]
                    this[chapterIndex] = oldChapter.copy(
                        lastWatchedCharIndex = charIndex,
                        readed = true
                    )
                }
                this.chapters = chapters.copy(chapters = newChapters)
            }
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
            backHandler.register(backCallback)
            val state = state.value
            if(state.text.isEmpty() && !state.error) {
                coroutineScope.launch {
                    openChapter(initialChapterIndex)
                }
            }
        }
        lifecycle.doOnDestroy {
            backHandler.unregister(backCallback)
        }
    }

    companion object {
        private const val INDENT_SPACES = "\u00A0\u00A0\u00A0\u00A0"
    }
}
