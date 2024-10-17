package ru.blays.ficbook.components.fanficPage.reader2

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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ru.blays.ficbook.components.fanficPage.reader.ReaderBottomContent
import ru.blays.ficbook.components.fanficPage.reader.ReaderControl
import ru.blays.ficbook.components.fanficPage.reader.ReaderTopBarContent
import ru.blays.ficbook.components.fanficPage.reader.TextSplitterConfig
import ru.blays.ficbook.platformUtils.FullscreenContainer
import ru.blays.ficbook.reader.shared.components.readerComponents.declaration.MainReaderComponent
import ru.blays.ficbook.reader.shared.components.readerComponents.declaration.VoteReaderComponent
import ru.blays.ficbook.theme.ReaderTheme

@Composable
fun FanficReaderContent2(
    component: MainReaderComponent
) {
    val state by component.state.subscribeAsState()
    val settings = state.settings

    FullscreenContainer(state.settings.fullscreenMode) {
        Column {
            var readerState: ReaderState? by remember {
                mutableStateOf(null)
            }

            ReaderTopBarContent(
                modifier = Modifier.requiredHeight(35.dp),
                state = state
            )
            ReaderTheme(
                darkTheme = settings.nightMode,
                darkColor = Color(settings.darkColor),
                lightColor = Color(settings.lightColor)
            ) {
                Reader(
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxWidth(),
                    voteComponent = component.voteComponent,
                    state = state
                ) {
                    readerState = it
                }
            }
            ReaderBottomContent(
                readerState = readerState,
                modifier = Modifier.requiredHeight(35.dp)
            )
        }
    }
}

@Composable
private fun Reader(
    modifier: Modifier = Modifier,
    voteComponent: VoteReaderComponent,
    state: MainReaderComponent.State,
    onNewState: (ReaderState) -> Unit
) {
    val density = LocalDensity.current

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
        modifier = modifier.background(MaterialTheme.colorScheme.background)
    ) {
        val maxWidth = constraints.maxWidth.toFloat()
        val pagesGapPx = with(density) { PagesGap.toPx() }

        val config = remember(style) {
            TextSplitterConfig.SinglePanelConfig(
                style = style,
                constraints = constraints
            )
        }

        val readerState = rememberReaderState(
            state.chapterIndex,
            text = text,
            config = config
        )

        LaunchedEffect(readerState) {
            onNewState(readerState)
        }

        var isDragged by remember { mutableStateOf(false) }
        var pageOffsetX by remember { mutableFloatStateOf(0F) }
        val controlExpanded = remember { mutableStateOf(false) }

        if(readerState.pages.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { isDragged = true },
                            onDragEnd = { isDragged = false },
                            onDragCancel = { isDragged = false },
                            onDrag = { _, dragAmount ->
                                when {
                                    dragAmount.x > 0 -> {
                                        if(readerState.hasPreviousPage) {
                                            pageOffsetX += dragAmount.x
                                        } else {
                                            pageOffsetX = (pageOffsetX + dragAmount.x).coerceAtMost(0F)
                                        }
                                    }
                                    dragAmount.x < 0 -> {
                                        if(readerState.hasNextPage) {
                                            pageOffsetX += dragAmount.x
                                        } else {
                                            pageOffsetX = (pageOffsetX + dragAmount.x).coerceAtLeast(0F)
                                        }
                                    }
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures {
                            val tapX = it.x
                            val width = size.width
                            when {
                                tapX < width * 0.33F -> {
                                    if(readerState.hasPreviousPage) readerState.previousPage()
                                }
                                tapX < width * 0.66F -> {
                                    controlExpanded.value = !controlExpanded.value
                                }
                                else -> {
                                    if(readerState.hasNextPage) readerState.nextPage()
                                }
                            }
                        }
                    }
            ) {
                if(readerState.hasPreviousPage) {
                    Page(
                        modifier = Modifier.graphicsLayer {
                            translationX = -(maxWidth + pagesGapPx) + pageOffsetX
                        },
                        page = readerState.pages[readerState.pageIndex - 1],
                        style = style,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Page(
                    modifier = Modifier.graphicsLayer {
                        translationX = pageOffsetX
                    },
                    page = readerState.pages[readerState.pageIndex],
                    style = style,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if(readerState.hasNextPage) {
                    Page(
                        modifier = Modifier.graphicsLayer {
                            translationX = (maxWidth + pagesGapPx) + pageOffsetX
                        },
                        page = readerState.pages[readerState.pageIndex + 1],
                        style = style,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            LaunchedEffect(isDragged) {
                if(isDragged) return@LaunchedEffect
                when {
                    pageOffsetX > (maxWidth * 0.33F) -> {
                        if(readerState.hasPreviousPage) {
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
                    pageOffsetX < -(maxWidth * 0.33F) -> {
                        if(readerState.hasNextPage) {
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
            openSettings = {

            }
        )
    }
}

@Composable
fun Page(
    modifier: Modifier = Modifier,
    page: AnnotatedString,
    style: TextStyle,
    color: Color
) {
    Text(
        modifier = modifier,
        text = page,
        style = style,
        color = color
    )
}

private val PagesGap = 12.dp