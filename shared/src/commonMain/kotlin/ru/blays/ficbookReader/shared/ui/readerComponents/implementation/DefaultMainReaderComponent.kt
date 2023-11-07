package ru.blays.ficbookReader.shared.ui.readerComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.slot.*
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.realm.kotlin.Realm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
import ru.blays.ficbookReader.shared.data.dto.FanficChapterStable
import ru.blays.ficbookReader.shared.data.realm.entity.ChapterEntity
import ru.blays.ficbookReader.shared.di.injectRealm
import ru.blays.ficbookReader.shared.preferences.repositiry.ISettingsJsonRepository
import ru.blays.ficbookReader.shared.ui.readerComponents.declaration.MainReaderComponent
import ru.blays.ficbookReader.shared.ui.readerComponents.declaration.SettingsReaderComponent
import ru.blays.ficbookapi.ficbookConnection.IFicbookApi
import ru.blays.ficbookapi.result.ApiResult

class DefaultMainReaderComponent(
    componentContext: ComponentContext,
    private val ficbookApi: IFicbookApi,
    private val chapters: List<FanficChapterStable>,
    initialChapterIndex: Int,
    fanficID: String,
    private val output: (output: MainReaderComponent.Output) -> Unit
) : MainReaderComponent, ComponentContext by componentContext {
    private val settingsJsonRepository: ISettingsJsonRepository by inject(ISettingsJsonRepository::class.java)

    private var readerSettings: MainReaderComponent.Settings by settingsJsonRepository.getDelegate(
        serializer = MainReaderComponent.Settings.serializer(),
        defaultValue = MainReaderComponent.Settings(),
        key = "ReaderPrefs"
    )

    private val _state: MutableValue<MainReaderComponent.State> = MutableValue(
        MainReaderComponent.State(
            chapterIndex = initialChapterIndex,
            chaptersCount = chapters.size,
            chapterName = chapters[initialChapterIndex].run {
                if (this is FanficChapterStable.SeparateChapterModel) name else "Глава 1"
            },
            text = "",
            loading = true,
            error = false,
            settings = readerSettings
        )
    )
    override val state: Value<MainReaderComponent.State>
        get() = _state

    private val dialogNavigation = SlotNavigation<MainReaderComponent.SettingsDialogConfig>()
    override val dialog: Value<ChildSlot<*, SettingsReaderComponent>> = childSlot(
        serializer = MainReaderComponent.SettingsDialogConfig.serializer(),
        source = dialogNavigation,
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
        ficbookApi = ficbookApi,
        chapters = chapters,
        fanficID = fanficID
    )

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        openChapter(initialChapterIndex)
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
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
                println("Intent received")
                saveReadProgress(
                    chapter = chapters[intent.chapterIndex],
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
        if (index in chapters.indices) {
            val realm: Realm = injectRealm(ChapterEntity::class)
            when (
                val chapter = chapters[index]
            ) {
                is FanficChapterStable.SeparateChapterModel -> {
                    _state.update {
                        it.copy(
                            loading = true,
                            text = ""
                        )
                    }
                    val savedChapter = realm.write {
                        query(ChapterEntity::class)
                            .query(
                                "href = $0", chapter.href
                            )
                            .first()
                            .find()
                    }

                    if(savedChapter?.text?.isNotEmpty() == true) {
                        _state.update {
                            it.copy(
                                text = savedChapter.text,
                                loading = false,
                                chapterIndex = index,
                                initialCharIndex = savedChapter.lastWatchedCharIndex,
                                chapterName = chapter.name,
                            )
                        }
                        return@launch
                    }
                    val apiResult = ficbookApi.getFanficChapterText(chapter.href)
                    when (apiResult) {
                        is ApiResult.Success -> {
                            val text = apiResult.value
                            _state.update {
                                it.copy(
                                    text = text,
                                    loading = false,
                                    chapterIndex = index,
                                    initialCharIndex = savedChapter?.lastWatchedCharIndex ?: 0,
                                    chapterName = chapter.name,
                                )
                            }
                            realm.write {
                                copyToRealm(
                                    ChapterEntity(
                                        name = chapter.name,
                                        href = chapter.href,
                                        text = text,
                                        lastWatchedCharIndex = 0,
                                        readed = false
                                    )
                                )
                            }
                        }
                        is ApiResult.Error -> {
                            val message = apiResult.message
                            _state.update {
                                it.copy(
                                    text = message,
                                    loading = false,
                                )
                            }
                        }
                    }
                }
                is FanficChapterStable.SingleChapterModel -> {
                    _state.update {
                        it.copy(
                            loading = false,
                            text = chapter.text
                        )
                    }
                }
            }
        }
    }

    private fun saveReadProgress(
        chapter: FanficChapterStable,
        charIndex: Int
    ) = CoroutineScope(Dispatchers.IO).launch {
        if (chapter is FanficChapterStable.SeparateChapterModel) {
            val realm: Realm = injectRealm(ChapterEntity::class)
            realm.write {
                val savedChapter = query(ChapterEntity::class).query(
                    "href = $0", chapter.href
                )
                .first()
                .find()

                if (savedChapter != null) {
                    savedChapter.lastWatchedCharIndex = charIndex
                } else {
                    copyToRealm(
                        ChapterEntity(
                            href = chapter.href,
                            name = chapter.name,
                            lastWatchedCharIndex = charIndex,
                            readed = true,
                            text = ""
                        )
                    )
                }
            }
        }
    }
}
