package ru.blays.ficbookReader.components.fanficPage.reader

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.example.myapplication.compose.Res
import io.github.skeptick.libres.compose.painterResource
import kotlinx.coroutines.launch
import ru.blays.ficbookReader.platformUtils.FullscreenContainer
import ru.blays.ficbookReader.shared.ui.readerComponents.declaration.MainReaderComponent
import ru.blays.ficbookReader.shared.ui.readerComponents.declaration.SettingsReaderComponent
import ru.blays.ficbookReader.theme.ReaderTheme
import ru.blays.ficbookReader.values.DefaultPadding
import ru.hh.toolbar.custom_toolbar.CollapsedToolbar
import ru.hh.toolbar.custom_toolbar.CollapsingTitle

@OptIn(ExperimentalFoundationApi::class)
@Composable
actual fun LandscapeContent(component: MainReaderComponent) {
    val state by component.state.subscribeAsState()
    val text = remember(state) { state.text }
    val settings = remember(state) { state.settings }

    val settingsSlot by component.dialog.subscribeAsState()

    Scaffold(
        topBar = {
            CollapsedToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.onOutput(
                                MainReaderComponent.Output.NavigateBack
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Иконка стрелка назад"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            component.onIntent(
                                MainReaderComponent.Intent.OpenCloseSettings
                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.image.ic_settings),
                            contentDescription = "Иконка настроек",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.small(
                    titleText = state.chapterName
                )
            )
        }
    ) { padding ->
        var pagerState: State<PagerState?> = remember { mutableStateOf(null) }
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val slotInstance = settingsSlot.child?.instance
            val twoPanel = maxWidth > 1000.dp
            Column(
                modifier = Modifier
                    .padding(top = padding.calculateTopPadding())
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                ReaderTheme(
                    darkTheme = settings.nightMode,
                    darkColor = Color(settings.darkColor),
                    lightColor = Color(settings.lightColor)
                ) {
                    pagerState = ReaderContentDesktop(
                        text = text,
                        settings = state.settings,
                        modifier = Modifier.fillMaxHeight(0.9F),
                        twoPanel = twoPanel,
                        onCenterZoneClick = {}
                    )
                }
                if (pagerState.value != null) {
                    BottomControlLandscape(
                        pagerState = pagerState.value!!,
                        readerState = state,
                        modifier = Modifier.fillMaxHeight(),
                        openNextChapter = {
                            component.onIntent(
                                MainReaderComponent.Intent.ChangeChapter(state.chapterIndex + 1)
                            )
                        },
                        openPreviousChapter = {
                            component.onIntent(
                                MainReaderComponent.Intent.ChangeChapter(state.chapterIndex - 1)
                            )
                        }
                    )
                }
            }
            AnimatedContent(
                targetState = slotInstance,
                transitionSpec = {
                    slideInHorizontally(spring()) togetherWith slideOutHorizontally(spring())
                }
            ) {
                if(it != null) {
                    LandscapeSettings(
                        component = it,
                        modifier = Modifier
                            .padding(top = padding.calculateTopPadding())
                            .width(250.dp)
                    )
                }
            }
        }
    }
}

@Composable
actual fun PortraitContent(component: MainReaderComponent) = LandscapeContent(component)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReaderContentDesktop(
    modifier: Modifier = Modifier,
    text: String,
    twoPanel: Boolean,
    settings: MainReaderComponent.Settings,
    onCenterZoneClick: () -> Unit
): State<PagerState?> {
    val baseStyle = MaterialTheme.typography.bodyMedium
    val style = remember(settings.fontSize) {
        baseStyle.copy(
            fontSize = settings.fontSize.sp
        )
    }

    val pagerState: MutableState<PagerState?> = remember { mutableStateOf(null) }

    FullscreenContainer(
        enabled = settings.fullscreenMode
    ) {
        if(text.isNotEmpty()) {
            BoxWithConstraints(
                modifier = modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(DefaultPadding.CardDefaultPadding)
            ) {
                if (twoPanel) {
                    //println("Used two panel reader")
                    val density = LocalDensity.current
                    val config = remember(style, constraints.maxHeight, constraints.maxWidth) {
                        TextSplitterConfig.TwoPanelConfig(
                            style = style,
                            constraints = constraints,
                            spaceBetweenPanel = with(density) { 50.dp.roundToPx() }
                        )
                    }
                    println("""
                        Current max height: ${config.constraints.maxHeight}
                        Current max width: ${config.constraints.maxWidth}
                        """.trimIndent()
                    )
                    val pages = rememberTwoPanelTextPages(
                        text = text,
                        config = config
                    )
                    pagerState.value = rememberPagerState {
                        pages.size
                    }
                    TwoPanelPager(
                        pagerState = pagerState.value!!,
                        pages = pages,
                        config = config,
                        onCenterZoneClick = onCenterZoneClick
                    )
                } else {
                    //println("Used single panel reader")
                    val pages = rememberTextPages(
                        text = text,
                        config = TextSplitterConfig.SinglePanelConfig(
                            style = style,
                            constraints = constraints
                        )
                    )
                    pagerState.value = rememberPagerState {
                        pages.size
                    }
                    SinglePanelPager(
                        pagerState = pagerState.value!!,
                        pages = pages,
                        config = TextSplitterConfig.SinglePanelConfig(
                            style = style,
                            constraints = constraints
                        ),
                        onCenterZoneClick = onCenterZoneClick
                    )
                }
            }
        }
    }
    return pagerState
}

@Composable
fun SinglePanelPager(
    pagerState: PagerState,
    pages: List<String>,
    config: TextSplitterConfig.SinglePanelConfig,
    onCenterZoneClick: () -> Unit
) {
    TextPager(
        pagerState = pagerState,
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

@Composable
fun TwoPanelPager(
    pagerState: PagerState,
    pages: List<Pair<String, String>>,
    config: TextSplitterConfig.TwoPanelConfig,
    onCenterZoneClick: () -> Unit
) {
    TextPager(
        pagerState = pagerState,
        onCenterZoneClick = onCenterZoneClick
    ) { page ->
        val (firstPanelText, secondPanelText) = pages[page]
        SubcomposeLayout(
            modifier = Modifier.fillMaxSize()
        ) { constraints ->
            val onePanelSize = (constraints.maxWidth-config.spaceBetweenPanel)/2
            val firstPanel = subcompose("firstPanel") {
                Text(
                    text = firstPanelText,
                    style = config.style,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            val secondPanel = subcompose("secondPanel") {
                Text(
                    text = secondPanelText,
                    style = config.style,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            layout(constraints.maxWidth, constraints.minHeight) {
                firstPanel.first()
                    .measure(
                        constraints.copy(
                            maxWidth = onePanelSize,
                            minWidth = onePanelSize
                        )
                    )
                    .place(0, 0)

                secondPanel.first()
                    .measure(
                        constraints.copy(
                            maxWidth = onePanelSize,
                            minWidth = onePanelSize
                        )
                    )
                    .place(
                        x = onePanelSize + config.spaceBetweenPanel,
                        y = 0
                    )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
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
            .onPointerEvent(PointerEventType.Scroll) {
                val newPage = pagerState.currentPage + it.changes.first().scrollDelta.y.toInt()
                scope.launch {
                    pagerState.animateScrollToPage(newPage)
                }
            }
    ) { page ->
        pagerContent(page)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BottomControlLandscape(
    pagerState: PagerState,
    readerState: MainReaderComponent.State,
    modifier: Modifier = Modifier,
    openNextChapter: () -> Unit,
    openPreviousChapter: () -> Unit
) {
    val hasPreviousPage = pagerState.canScrollBackward
    val hasNextPage = pagerState.canScrollForward

    val scope = rememberCoroutineScope()

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.weight(1F/5F),
            contentAlignment = Alignment.Center
        ) {
            NextChapterButton(
                visible = !hasPreviousPage,
                icon = Icons.Rounded.ArrowBack,
                contentDescription = "Кнопка предыдущая глава",
                onClick = openPreviousChapter
            )
        }
        Box(
            modifier = Modifier.weight(3F/5F)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${pagerState.currentPage+1}/${pagerState.pageCount}"
                )
                Spacer(modifier = Modifier.width(12.dp))
                Slider(
                    modifier = Modifier.weight(0.7F),
                    value = pagerState.currentPage.toFloat(),
                    onValueChange = {
                        scope.launch {
                            pagerState.scrollToPage(it.toInt())
                        }
                    },
                    valueRange = (
                            0F..(pagerState.pageCount-1).coerceAtLeast(1).toFloat()
                            )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${pagerState.currentPage+1 percentageOf pagerState.pageCount}%"
                )
            }
        }
        Box(
            modifier = Modifier.weight(1F/5F),
            contentAlignment = Alignment.Center
        ) {
            NextChapterButton(
                visible = !hasNextPage,
                icon = Icons.Rounded.ArrowForward,
                contentDescription = "Кнопка следующая глава",
                onClick = openNextChapter
            )
        }
    }
}

@Composable
private fun NextChapterButton(
    visible: Boolean,
    icon: ImageVector,
    contentDescription: String? = null,
    onClick: () -> Unit
) {
    AnimatedContent(
        targetState = visible
    ) { state ->
        if (state) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .layout { measurable, constraints ->
                        val maxSize = 60

                        layout(maxSize, maxSize) {
                            measurable
                                .measure(
                                    Constraints(
                                        maxWidth = maxSize,
                                        maxHeight = maxSize,
                                        minWidth = maxSize,
                                        minHeight = maxSize
                                    )
                                )
                                .place(0, -(maxSize/2))
                        }
                    }
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .clickable(onClick = onClick),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth())
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun LandscapeSettings(
    component: SettingsReaderComponent,
    modifier: Modifier = Modifier
) {
    val state by component.state.subscribeAsState()
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Цвета",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        color = Color(state.darkColor),
                        shape = CardDefaults.shape
                    )
                    .clip(CardDefaults.shape)
                    .clickable {

                    }
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text("Тёмный цвет")
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        color = Color(state.lightColor),
                        shape = CardDefaults.shape
                    )
                    .clip(CardDefaults.shape)
                    .clickable {

                    }
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text("Светлый цвет")
        }
        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Размер шрифта",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        FlowRow {
            for(size in 8..18) {
                InputChip(
                    selected = state.fontSize == size,
                    onClick = {
                        component.sendIntent(
                            SettingsReaderComponent.Intent.FontSizeChanged(size)
                        )
                    },
                    label = {
                        Text(size.toString())
                    },
                    shape = CircleShape,
                    modifier = Modifier/*.padding(2.dp)*/
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.padding(horizontal = 10.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Тёмная тема"
            )
            Switch(
                checked = state.nightMode,
                onCheckedChange = {
                    component.sendIntent(
                        SettingsReaderComponent.Intent.NightModeChanged(it)
                    )
                }
            )
        }
    }
}