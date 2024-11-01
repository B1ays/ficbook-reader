package ru.blays.ficbook.components.fanficPage.reader

import android.content.res.Configuration
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ru.blays.ficbook.platformUtils.FullscreenContainer
import ru.blays.ficbook.reader.shared.components.readerComponents.declaration.MainReaderComponent
import ru.blays.ficbook.reader.shared.components.readerComponents.declaration.VoteReaderComponent
import ru.blays.ficbook.theme.ReaderTheme


@Composable
actual fun FanficReaderContent(component: MainReaderComponent) {
    val state by component.state.subscribeAsState()
    val settings = state.settings

    val dialogInstance = component.dialog.subscribeAsState().value.child?.instance

    FullscreenContainer(
        enabled = settings.fullscreenMode
    ) {
        ReaderTheme(
            darkTheme = settings.nightMode,
            darkColor = Color(settings.darkColor),
            lightColor = Color(settings.lightColor)
        ) {
            Column(modifier = Modifier.systemBarsPadding()) {
                val readerState: MutableState<ReaderState?> = remember {
                    mutableStateOf(null)
                }

                ReaderTopBarContent(
                    modifier = Modifier.requiredHeight(35.dp),
                    state = state
                )

                Reader(
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxWidth(),
                    voteComponent = component.voteComponent,
                    state = state,
                    readerState = readerState,
                    onPreviousChapter = {
                        component.sendIntent(
                            MainReaderComponent.Intent.ChangeChapter(state.chapterIndex - 1)
                        )
                    },
                    onNextChapter = {
                        component.sendIntent(
                            MainReaderComponent.Intent.ChangeChapter(state.chapterIndex + 1)
                        )
                    },
                    onOpenSettings = {
                        component.sendIntent(
                            MainReaderComponent.Intent.ChangeDialogVisible
                        )
                    },
                    onDispose = { chapterIndex, charIndex ->
                        component.sendIntent(
                            MainReaderComponent.Intent.SaveProgress(chapterIndex, charIndex)
                        )
                    }
                )

                ReaderBottomContent(
                    readerState = readerState.value,
                    modifier = Modifier.requiredHeight(35.dp)
                )
            }

            dialogInstance?.let { dialogComponent ->
                ReaderSettingPopup(
                    component = dialogComponent,
                    readerSettingsModel = settings,
                    closeDialog = {
                        component.sendIntent(
                            MainReaderComponent.Intent.ChangeDialogVisible
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun Reader(
    modifier: Modifier = Modifier,
    voteComponent: VoteReaderComponent,
    state: MainReaderComponent.State,
    readerState: MutableState<ReaderState?>,
    onPreviousChapter: () -> Unit,
    onNextChapter: () -> Unit,
    onOpenSettings: () -> Unit,
    onDispose: (chapterIndex: Int, charIndex: Int) -> Unit
) {
    val density = LocalDensity.current
    val localView = LocalView.current
    val localConfig = LocalConfiguration.current
    val volumeKeysEventSource = LocalVolumeKeysEventSource.current

    val settings = state.settings

    val text = remember(state.text) {
        AnnotatedString.fromHtml(state.text)
    }

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

    BoxWithConstraints(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(10.dp)
    ) {
        val maxWidth = constraints.maxWidth.toFloat()
        val pagesGapPx = with(density) { PagesGap.toPx() }

        val config = remember(style, constraints) {
            TextSplitterConfig.SinglePanelConfig(
                style = style,
                constraints = constraints
            )
        }

        readerState.value = rememberReaderState(
            state.chapterIndex,
            text = text,
            initialCharIndex = state.initialCharIndex,
            config = config,
            onDispose = { charIndex ->
                onDispose(state.chapterIndex, charIndex)
            }
        )

        readerState.value?.let { readerState ->
            val hasPreviousPage = readerState.hasPreviousPage
            val hasNextPage = readerState.hasNextPage

            val pages = readerState.pages
            val pageIndex = readerState.pageIndex

            val hasPreviousChapter by remember {
                derivedStateOf { state.chapterIndex > 0 }
            }
            val hasNextChapter by remember {
                derivedStateOf { state.chapterIndex < (state.chaptersCount - 1) }
            }

            var isDragged by remember { mutableStateOf(false) }
            var pageOffsetX by remember { mutableFloatStateOf(0F) }
            val controlExpanded = remember { mutableStateOf(false) }

            if (pages.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .pointerInput(readerState) {
                            detectDragGestures(
                                onDragStart = { isDragged = true },
                                onDragEnd = { isDragged = false },
                                onDragCancel = { isDragged = false },
                                onDrag = { _, dragAmount ->
                                    when {
                                        dragAmount.x > 0 -> {
                                            if (readerState.hasPreviousPage) {
                                                pageOffsetX += dragAmount.x
                                            } else {
                                                pageOffsetX = (pageOffsetX + dragAmount.x).coerceAtMost(0F)
                                            }
                                        }

                                        dragAmount.x < 0 -> {
                                            if (readerState.hasNextPage) {
                                                pageOffsetX += dragAmount.x
                                            } else {
                                                pageOffsetX = (pageOffsetX + dragAmount.x).coerceAtLeast(0F)
                                            }
                                        }
                                    }
                                }
                            )
                        }
                        .pointerInput(readerState) {
                            detectTapGestures {
                                val tapX = it.x
                                val width = size.width
                                when {
                                    tapX < width * 0.33F -> {
                                        if (settings.autoOpenNextChapter) {
                                            when {
                                                !readerState.hasPreviousPage && hasPreviousChapter -> {
                                                    onPreviousChapter()
                                                }

                                                readerState.hasPreviousPage -> {
                                                    readerState.previousPage()
                                                }
                                            }
                                        } else {
                                            if (readerState.hasPreviousPage) readerState.previousPage()
                                        }
                                    }

                                    tapX < width * 0.66F -> {
                                        controlExpanded.value = !controlExpanded.value
                                    }

                                    else -> {
                                        if (settings.autoOpenNextChapter) {
                                            when {
                                                !readerState.hasNextPage && hasNextChapter -> {
                                                    onNextChapter()
                                                }

                                                readerState.hasNextPage -> {
                                                    readerState.nextPage()
                                                }
                                            }
                                        } else {
                                            if (readerState.hasNextPage) readerState.nextPage()
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    if (hasPreviousPage) {
                        Page(
                            modifier = Modifier.graphicsLayer {
                                translationX = -(maxWidth + pagesGapPx) + pageOffsetX
                            },
                            text = pages[pageIndex - 1],
                            textStyle = style,
                            textColor = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Page(
                        modifier = Modifier.graphicsLayer {
                            translationX = pageOffsetX
                        },
                        text = pages[pageIndex],
                        textStyle = style,
                        textColor = MaterialTheme.colorScheme.onBackground
                    )
                    if (hasNextPage) {
                        Page(
                            modifier = Modifier.graphicsLayer {
                                translationX = (maxWidth + pagesGapPx) + pageOffsetX
                            },
                            text = pages[pageIndex + 1],
                            textStyle = style,
                            textColor = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                LaunchedEffect(isDragged) {
                    if (isDragged) return@LaunchedEffect
                    when {
                        pageOffsetX > (maxWidth * 0.2F) -> {
                            if (hasPreviousPage) {
                                animate(
                                    initialValue = pageOffsetX,
                                    targetValue = maxWidth + pagesGapPx,
                                    animationSpec = tween(200)
                                ) { value, _ ->
                                    pageOffsetX = value
                                }
                                readerState.previousPage()
                                pageOffsetX = 0F
                            }
                        }

                        pageOffsetX < -(maxWidth * 0.2F) -> {
                            if (hasNextPage) {
                                animate(
                                    initialValue = pageOffsetX,
                                    targetValue = -(maxWidth + pagesGapPx),
                                    animationSpec = tween(200)
                                ) { value, _ ->
                                    pageOffsetX = value
                                }
                                readerState.nextPage()
                                pageOffsetX = 0F
                            }
                        }

                        else -> {
                            animate(
                                initialValue = pageOffsetX,
                                targetValue = 0F,
                                animationSpec = tween(200)
                            ) { value, _ ->
                                pageOffsetX = value
                            }
                        }
                    }
                }
            }

            ReaderControl(
                modifier = Modifier.align(Alignment.BottomCenter),
                voteComponent = voteComponent,
                readerState = readerState,
                state = state,
                expanded = controlExpanded,
                onPreviousChapter = onPreviousChapter,
                onNextChapter = onNextChapter,
                onOpenSettings = onOpenSettings
            )

            if (settings.scrollWithVolumeButtons) {
                DisposableEffect(Unit) {
                    volumeKeysEventSource.collect { key ->
                        when (key) {
                            VOLUME_UP -> {
                                if (localConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                                    if (readerState.hasNextPage) readerState.nextPage()
                                } else {
                                    if (readerState.hasPreviousPage) readerState.previousPage()
                                }
                                true
                            }

                            VOLUME_DOWN -> {
                                if (localConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                                    if (readerState.hasPreviousPage) readerState.previousPage()
                                } else {
                                    if (readerState.hasNextPage) readerState.nextPage()
                                }
                                true
                            }

                            else -> false
                        }
                    }

                    onDispose(volumeKeysEventSource::dispose)
                }
            }
        }

        DisposableEffect(settings.keepScreenOn) {
            val oldValue = localView.keepScreenOn
            localView.keepScreenOn = settings.keepScreenOn

            onDispose { localView.keepScreenOn = oldValue }
        }
    }
}

@Composable
private fun Page(
    modifier: Modifier = Modifier,
    text: AnnotatedString,
    textStyle: TextStyle,
    textColor: Color
) = Text(
    modifier = modifier,
    text = text,
    style = textStyle,
    color = textColor
)

private val PagesGap = 12.dp