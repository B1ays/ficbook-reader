package ru.blays.ficbookReader.components.fanficPage.reader

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import kotlinx.coroutines.launch
import ru.blays.ficbookReader.platformUtils.rememberTimeObserver
import ru.blays.ficbookReader.shared.ui.readerComponents.declaration.MainReaderComponent
import ru.blays.ficbookReader.theme.ReaderTheme
import ru.blays.ficbookReader.values.DefaultPadding

@OptIn(ExperimentalFoundationApi::class)
@Composable
actual fun LandscapeContent(component: MainReaderComponent) {
    val state by component.state.subscribeAsState()
    val text = remember(state) { state.text }
    val settings = remember(state) { state.settings }

    val settingsSlot by component.dialog.subscribeAsState()
    val slotInstance = settingsSlot.child?.instance

    var pagerState: PagerState? by remember { mutableStateOf(null) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        ReaderTopBarContent(
            component = component,
            modifier = Modifier.weight(0.04F)
        )
        ReaderTheme(
            darkTheme = settings.nightMode,
            darkColor = Color(settings.darkColor),
            lightColor = Color(settings.lightColor)
        ) {
            pagerState = ReaderContent(
                modifier = Modifier.weight(0.92F),
                text = text,
                settings = settings
            ) {
                // TODO
            }
        }
        pagerState?.let {
            ReaderBottomContent(
                pagerState = it,
                component = component,
                modifier = Modifier.weight(0.04F)
            )
        }
    }

}

@Composable
actual fun PortraitContent(component: MainReaderComponent) = LandscapeContent(component)


@Composable
private fun ReaderContent(
    modifier: Modifier = Modifier,
    text: String,
    settings: MainReaderComponent.Settings,
    onCenterZoneClick: () -> Unit
): PagerState? {
    val baseStyle = MaterialTheme.typography.bodyMedium
    val style = remember(settings.fontSize) {
        baseStyle.copy(
            fontSize = settings.fontSize.sp
        )
    }
    var pagerState: PagerState? by remember { mutableStateOf(null) }
    if(text.isNotEmpty()) {
        BoxWithConstraints(
            modifier = modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(DefaultPadding.CardDefaultPadding)
        ) {
            val pages = rememberTextPages(
                text = text,
                config = TextSplitterConfig.SinglePanelConfig(
                    style = style,
                    constraints = constraints
                )
            )
            pagerState = rememberPagerState {
                pages.size
            }
            val config = remember(style, constraints.maxHeight, constraints.maxWidth) {
                TextSplitterConfig.SinglePanelConfig(
                    style = style,
                    constraints = constraints
                )
            }
            TextPager(
                pagerState = pagerState!!,
                onCenterZoneClick = onCenterZoneClick
            ) { page ->
                Text(
                    text = pages[page],
                    style = config.style,
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }

    return pagerState
}

@Composable
private fun TextPager(
    pagerState: PagerState,
    onCenterZoneClick: () -> Unit,
    pagerContent: @Composable (page: Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    HorizontalPager(
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
                                pagerState.scrollToPage(
                                    pagerState.currentPage - 1
                                )
                            }
                        }
                        in midTapZone -> onCenterZoneClick()
                        in secondTapZone -> {
                            scope.launch {
                                pagerState.scrollToPage(
                                    pagerState.currentPage + 1
                                )
                            }
                        }
                    }
                }
            }
    ) { page ->
        pagerContent(page)
    }
}


@Composable
private fun ReaderTopBarContent(
    component: MainReaderComponent,
    modifier: Modifier
) {
    val state by component.state.subscribeAsState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.background, //TODO "surfaceContainerLowest"
                shape = RoundedCornerShape(
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                )
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "${state.chapterIndex+1}/${state.chaptersCount}",
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            modifier = Modifier,
            text = state.chapterName,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.width(12.dp))
    }
}

@Composable
private fun ReaderBottomContent(
    pagerState: PagerState,
    component: MainReaderComponent,
    modifier: Modifier
) {
    val time by rememberTimeObserver()
    val state by component.state.subscribeAsState()

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.background, //TODO "surfaceContainerLowest"
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp
                )
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "${pagerState.currentPage percentageOf pagerState.pageCount}%",
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = "${pagerState.currentPage}/${pagerState.pageCount}",
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = time,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.width(12.dp))
    }
}