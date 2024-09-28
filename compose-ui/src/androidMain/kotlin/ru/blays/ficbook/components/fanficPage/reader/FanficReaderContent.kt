package ru.blays.ficbook.components.fanficPage.reader

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.slot.ChildSlot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.blays.ficbook.platformUtils.FullscreenContainer
import ru.blays.ficbook.reader.shared.components.readerComponents.declaration.MainReaderComponent
import ru.blays.ficbook.reader.shared.components.readerComponents.declaration.SettingsReaderComponent
import ru.blays.ficbook.theme.ReaderTheme
import ru.blays.ficbook.ui_components.pager2.*
import ru.blays.ficbook.values.DefaultPadding
import kotlin.math.roundToInt

@Composable
actual fun FanficReaderContent(component: MainReaderComponent) {
    val state = component.state.subscribeAsState()
    val settingsSlot = component.dialog.subscribeAsState()
    val controlEventSource = remember { MutableStateFlow(false) }

    val reader = Reader.rememberReader(
        settingsSlot = settingsSlot,
        state = state,
        onDispose = { charIndex ->
            component.sendIntent(
                MainReaderComponent.Intent.SaveProgress(
                    chapterIndex = state.value.chapterIndex,
                    charIndex = charIndex
                )
            )
        },
        onCenterZoneClick = {
            controlEventSource.value = !controlEventSource.value
        },
        openPreviousChapter = {
            component.sendIntent(
                MainReaderComponent.Intent.ChangeChapter(
                    chapterIndex = state.value.chapterIndex - 1
                )
            )
        },
        openNextChapter = {
            component.sendIntent(
                MainReaderComponent.Intent.ChangeChapter(
                    chapterIndex = state.value.chapterIndex + 1
                )
            )
        },
        openSettings = {
            component.sendIntent(
                MainReaderComponent.Intent.OpenOrCloseSettings
            )
        }
    )
    reader(
        component = component,
        controlEventSource = controlEventSource,
        modifier = Modifier.fillMaxSize()
    )
}

private class Reader(
    private val settingsSlot: State<ChildSlot<*, SettingsReaderComponent>>,
    private val state: State<MainReaderComponent.State>,
    private val onDispose: (charIndex: Int) -> Unit,
    private val onCenterZoneClick: () -> Unit,
    private val openPreviousChapter: () -> Unit,
    private val openNextChapter: () -> Unit,
    private val openSettings: () -> Unit
) {
    val pagerState: PagerState2<AnnotatedString> = PagerStateImpl2(
        initialPages = emptyList(),
        initialPageIndex = 0,
        initialPageOffsetFraction = 0f
    )

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val currentProgress: Float
        get() = pagerState.currentPage / pagerState.pageCount.toFloat()

    
    @Composable
    operator fun invoke(
        component: MainReaderComponent,
        controlEventSource: MutableStateFlow<Boolean>,
        modifier: Modifier = Modifier
    ) {
        val currentState by state
        val text = currentState.text
        val annotatedString = remember(text) {
            AnnotatedString.fromHtml(text)
        }
        val settings = currentState.settings

        val slotState by settingsSlot
        val slotInstance = slotState.child?.instance

        FullscreenContainer(
            enabled = settings.fullscreenMode
        ) {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                ReaderTopBarContent(
                    component = component,
                    modifier = Modifier.requiredHeight(35.dp)
                )
                Box(
                    modifier = Modifier.weight(1F),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    ReaderTheme(
                        darkTheme = settings.nightMode,
                        darkColor = Color(settings.darkColor),
                        lightColor = Color(settings.lightColor)
                    ) {
                        ReaderContent(
                            modifier = Modifier,
                            text = annotatedString,
                            settings = settings,
                            onCenterZoneClick = onCenterZoneClick
                        )
                    }
                    ReaderControl(
                        voteComponent = component.voteComponent,
                        pagerState = pagerState,
                        readerState = currentState,
                        eventSource = controlEventSource,
                        modifier = Modifier,
                        openPreviousChapter = openPreviousChapter,
                        openNextChapter = openNextChapter,
                        openSettings = openSettings
                    )
                }
                ReaderBottomContent(
                    pagerState = pagerState,
                    modifier = Modifier.requiredHeight(35.dp)
                )
            }
            slotInstance?.let { dialogComponent ->
                ReaderSettingPopup(
                    component = dialogComponent,
                    readerSettingsModel = settings,
                    closeDialog = {
                        component.sendIntent(
                            MainReaderComponent.Intent.OpenOrCloseSettings
                        )
                    }
                )
            }
        }
    }

    @Composable
    private fun ReaderContent(
        modifier: Modifier = Modifier,
        text: AnnotatedString,
        settings: MainReaderComponent.Settings,
        onCenterZoneClick: () -> Unit
    ) {
        val baseStyle = MaterialTheme.typography.bodyMedium
        val style = remember(
            settings.fontSize,
            settings.lineHeight
        ) {
            baseStyle.copy(
                fontSize = settings.fontSize.sp,
                lineHeight = settings.lineHeight.sp,
            )
        }
        val scope = rememberCoroutineScope()
        val textMeasurer = rememberTextMeasurer()
        val localConfig = LocalConfiguration.current
        val volumeKeysEventSource = LocalVolumeKeysEventSource.current
        val localView = LocalView.current

        BoxWithConstraints(
            modifier = modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(DefaultPadding.CardDefaultPadding)
        ) {
            val config = remember(
                style,
                constraints.maxHeight,
                constraints.maxWidth
            ) {
                TextSplitterConfig.SinglePanelConfig(
                    style = style,
                    constraints = constraints
                )
            }

            DisposableEffect(
                text,
                config
            ) {
                val job = scope.launch {
                    val pages = splitTextToPages(
                        text = text,
                        config = config,
                        textMeasurer = textMeasurer
                    )
                    pagerState.updatePages { pages }
                }
                onDispose(job::cancel)
            }

            TextPager(
                pagerState = pagerState,
                onCenterZoneClick = onCenterZoneClick
            ) { _, page ->
                Text(
                    text = page,
                    style = config.style,
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if(settings.scrollWithVolumeButtons) {
                DisposableEffect(Unit) {
                    volumeKeysEventSource.collect { key ->
                        when(key) {
                            VOLUME_UP -> {
                                scope.launch {
                                    if(localConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                                        animatedScrollToNextPageOrChapter()
                                    } else {
                                        animatedScrollToPreviousPageOrChapter()
                                    }
                                }
                                true
                            }
                            VOLUME_DOWN -> {
                                scope.launch {
                                    if(localConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                        animatedScrollToNextPageOrChapter()
                                    } else {
                                        animatedScrollToPreviousPageOrChapter()
                                    }
                                }
                                true
                            }
                            else -> false
                        }
                    }

                    onDispose {
                        volumeKeysEventSource.dispose()
                    }
                }
            }

            DisposableEffect(settings.keepScreenOn) {
                localView.keepScreenOn = settings.keepScreenOn
                onDispose {
                    localView.keepScreenOn = false
                }
            }

            /*DisposableEffect(state.value.chapterIndex) {
                val pages = pagerState.pages
                scope.launch {
                    while(pagerState.pageCount == 0) {
                        delay(100)
                    }
                    val page = pages.findPageIndexForCharIndex(
                        index = state.value.initialCharIndex
                    )
                    if(page !in pages.indices) return@launch
                    pagerState.scrollToPage(page)
                }

                onDispose {
                    val absoluteCharIndex = pages.findCharIndexForPageIndex(pagerState.currentPage)
                    onDispose(absoluteCharIndex)
                }
            }*/
        }
    }

    @Composable
    private fun <T: Any> TextPager(
        pagerState: PagerState2<T>,
        onCenterZoneClick: () -> Unit,
        pagerContent: @Composable PagerScope.(index: Int, page: T) -> Unit
    ) {
        val scope = rememberCoroutineScope()
        HorizontalPager2(
            state = pagerState,
            modifier = Modifier
                .pointerInput(Unit) {
                    val firstTapZone = 0.0..size.width * (1.0/3)
                    val midTapZone = size.width * (1.0/3)..size.width * (2.0/3)
                    val secondTapZone = size.width * (2.0/3)..size.width.toDouble()
                    detectTapGestures {
                        when (it.x) {
                            in firstTapZone -> {
                                scope.launch {
                                    scrollToPreviousPageOrChapter()
                                }
                            }
                            in midTapZone -> onCenterZoneClick()
                            in secondTapZone -> {
                                scope.launch {
                                    scrollToNextPageOrChapter()
                                }
                            }
                        }
                    }
                },
            pageContent = pagerContent
        )
    }

    private suspend fun animatedScrollToNextPageOrChapter() {
        if(state.value.settings.autoOpenNextChapter) {
            if(!pagerState.canScrollForward) {
                openNextChapter()
            } else {
                pagerState.animatedScrollToNextPage()
            }
        } else {
            pagerState.animatedScrollToNextPage()
        }
    }

    private suspend fun animatedScrollToPreviousPageOrChapter() {
        if(state.value.settings.autoOpenNextChapter) {
            if(!pagerState.canScrollBackward) {
                openPreviousChapter()
            } else {
                pagerState.animatedScrollToPreviousPage()
            }
        } else {
            pagerState.animatedScrollToPreviousPage()
        }
    }

    private suspend fun scrollToNextPageOrChapter() {
        if(state.value.settings.autoOpenNextChapter) {
            if(!pagerState.canScrollForward) {
                openNextChapter()
            } else {
                pagerState.scrollToNextPage()
            }
        } else {
            pagerState.scrollToNextPage()
        }
    }

    private suspend fun scrollToPreviousPageOrChapter() {
        if(state.value.settings.autoOpenNextChapter) {
            if(!pagerState.canScrollBackward) {
                openPreviousChapter()
            } else {
                pagerState.scrollToPreviousPage()
            }
        } else {
            pagerState.scrollToPreviousPage()
        }
    }

    @JvmName("findPageIndexForCharIndexSinglePanel")
    private fun List<String>.findPageIndexForCharIndex(index: Int): Int {
        var currentIndex = 0
        for ((i, e) in withIndex()) {
            val pageLength = e.length
            if(index >= currentIndex && index < currentIndex + pageLength) {
                return i
            }
            currentIndex += pageLength
        }
        return 0
    }

    @JvmName("findCharIndexForPageIndexSinglePanel")
    private fun List<String>.findCharIndexForPageIndex(index: Int): Int {
        var currentIndex = 0
        for (i in 0 until index) {
            currentIndex += get(i).length
        }
        currentIndex += ((getOrNull(index)?.length ?: 0)/2)
        return currentIndex
    }

    private fun initialize(initialProgress: Float) {
        coroutineScope.launch {
            while(pagerState.pages.isEmpty()) {
                delay(300)
            }
            val page = (initialProgress * pagerState.pages.lastIndex)
                .roundToInt()
                .coerceIn(pagerState.pages.indices)
            pagerState.scrollToPage(page)
        }
    }
    
    companion object {
        @Composable
        fun rememberReader(
            settingsSlot: State<ChildSlot<*, SettingsReaderComponent>>,
            state: State<MainReaderComponent.State>,
            onDispose: (charIndex: Int) -> Unit,
            onCenterZoneClick: () -> Unit,
            openPreviousChapter: () -> Unit,
            openNextChapter: () -> Unit,
            openSettings: () -> Unit
        ): Reader = rememberSaveable(
            saver = listSaver<Reader, Any>(
                save = {
                    listOf(
                        it.currentProgress
                    )
                },
                restore = {
                    Reader(
                        settingsSlot = settingsSlot,
                        state = state,
                        onDispose = onDispose,
                        onCenterZoneClick = onCenterZoneClick,
                        openPreviousChapter = openPreviousChapter,
                        openNextChapter = openNextChapter,
                        openSettings = openSettings
                    ).apply {
                        initialize(it[0] as Float)
                    }
                }
            )
        ) {
            Reader(
                settingsSlot = settingsSlot,
                state = state,
                onDispose = onDispose,
                onCenterZoneClick = onCenterZoneClick,
                openPreviousChapter = openPreviousChapter,
                openNextChapter = openNextChapter,
                openSettings = openSettings
            )
        }
    }
}
