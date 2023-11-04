package ru.blays.ficbookReader.components.fanficPage.reader

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.arkivanov.decompose.router.slot.ChildSlot
import com.example.myapplication.compose.Res
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.blays.ficbookReader.platformUtils.FullscreenContainer
import ru.blays.ficbookReader.platformUtils.rememberTimeObserver
import ru.blays.ficbookReader.shared.ui.readerComponents.declaration.MainReaderComponent
import ru.blays.ficbookReader.shared.ui.readerComponents.declaration.SettingsReaderComponent
import ru.blays.ficbookReader.theme.ReaderTheme
import ru.blays.ficbookReader.ui_components.CustomButton.CustomIconButton
import ru.blays.ficbookReader.values.CardShape
import ru.blays.ficbookReader.values.DefaultPadding

@Composable
actual fun LandscapeContent(component: MainReaderComponent) {
    val state = component.state.subscribeAsState()
    val settingsSlot = component.dialog.subscribeAsState()
    val controlEventSource = remember { MutableStateFlow(false) }

    if(!state.value.loading) {
        val reader = remember(
            state.value.chapterIndex,
            state.value.initialCharIndex
        ) {
            Reader(
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
                }
            )
        }
        reader.Execute(
            component = component,
            controlEventSource = controlEventSource,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
actual fun PortraitContent(component: MainReaderComponent) = LandscapeContent(component)

private class Reader(
    private val settingsSlot: State<ChildSlot<*, SettingsReaderComponent>>,
    private val state: State<MainReaderComponent.State>,
    private val onDispose: (charIndex: Int) -> Unit,
    private val onCenterZoneClick: () -> Unit,
    private val openPreviousChapter: () -> Unit,
    private val openNextChapter: () -> Unit
) {
    var pagerState: PagerState? by mutableStateOf(null)
    var currentCharIndex = state.value.initialCharIndex


    @Composable
    fun Execute(
        component: MainReaderComponent,
        controlEventSource: MutableStateFlow<Boolean>,
        modifier: Modifier = Modifier
    ) {
        val currentState by state
        val text = currentState.text
        val settings = currentState.settings

        val slotInstance = settingsSlot.value.child?.instance

        FullscreenContainer(
            enabled = settings.fullscreenMode
        ) {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                ReaderTopBarContent(
                    component = component,
                    modifier = Modifier.weight(0.04F)
                )
                Box(
                    modifier = Modifier.weight(0.92F),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    ReaderTheme(
                        darkTheme = settings.nightMode,
                        darkColor = Color(settings.darkColor),
                        lightColor = Color(settings.lightColor)
                    ) {
                        ReaderContent(
                            modifier = Modifier,
                            text = text,
                            settings = settings,
                            onCenterZoneClick = onCenterZoneClick
                        )
                    }
                    pagerState?.let {
                        Control(
                            pagerState = it,
                            readerState = currentState,
                            eventSource = controlEventSource,
                            modifier = Modifier,
                            openPreviousChapter = openPreviousChapter,
                            openNextChapter = openNextChapter,
                            openSettings = onCenterZoneClick
                        )
                    }
                }
                pagerState?.let {
                    ReaderBottomContent(
                        pagerState = it,
                        modifier = Modifier.weight(0.04F)
                    )
                }
            }
            slotInstance?.let { dialogComponent ->
                ReaderSettingPopup(
                    component = dialogComponent,
                    readerSettingsModel = settings,
                ) {
                    component.sendIntent(
                        MainReaderComponent.Intent.OpenOrCloseSettings
                    )
                }
            }
        }
    }

    @SuppressLint("ComposableNaming")
    @Composable
    private fun ReaderContent(
        modifier: Modifier = Modifier,
        text: String,
        settings: MainReaderComponent.Settings,
        onCenterZoneClick: () -> Unit
    ) {
        val baseStyle = MaterialTheme.typography.bodyMedium
        val style = remember(settings.fontSize) {
            baseStyle.copy(
                fontSize = settings.fontSize.sp
            )
        }
        val scope = rememberCoroutineScope()

        BoxWithConstraints(
            modifier = Modifier
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

            DisposableEffect(Unit) {
                scope.launch {
                    while(pages.isEmpty()) {
                        delay(100)
                    }
                    val page = pages.findPageIndexForCharIndex(currentCharIndex)
                    pagerState?.scrollToPage(page.coerceAtMost(pages.lastIndex))
                }
                onDispose {
                    val pagerState = pagerState
                    if (pagerState != null) {
                        val absoluteCharIndex = pages.findCharIndexForPageIndex(pagerState.currentPage)
                        currentCharIndex = absoluteCharIndex
                        onDispose(absoluteCharIndex)
                    }
                }
            }
        }
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
        modifier: Modifier
    ) {
        val time by rememberTimeObserver()

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
                text = "${(pagerState.currentPage)+1}/${pagerState.pageCount}",
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

    @Composable
    private fun Control(
        pagerState: PagerState,
        readerState: MainReaderComponent.State,
        eventSource: Flow<Boolean>,
        modifier: Modifier = Modifier,
        openNextChapter: () -> Unit,
        openPreviousChapter: () -> Unit,
        openSettings: () -> Unit
    ) {
        val hasPreviousPage = pagerState.canScrollBackward
        val hasNextPage = pagerState.canScrollForward
        val hasNextChapter = readerState.chapterIndex < readerState.chaptersCount-1
        val hasPreviousChapter = readerState.chapterIndex > 0

        val previousButtonShowed = hasPreviousChapter && !hasPreviousPage
        val nextButtonShowed = hasNextChapter && !hasNextPage

        val scope = rememberCoroutineScope()

        var expanded by remember { mutableStateOf(false) }

        val shape = CardDefaults.shape

        LaunchedEffect(previousButtonShowed, nextButtonShowed) {
            if(previousButtonShowed) {
                expanded = true
            }
            if (nextButtonShowed) {
                expanded = true
            }
        }

        LaunchedEffect(Unit) {
            eventSource.collect { newValue ->
                expanded = newValue
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = slideInVertically(spring()) { it/2 }
                    + expandVertically(spring()),
            exit = slideOutVertically(spring()) { it/2 }
                    + shrinkVertically(spring()),
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(0.4F)
                        .defaultMinSize(
                            minHeight = 35.dp
                        )
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .clickable(onClick = openSettings)
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp).align(Alignment.Center),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(Res.image.ic_settings),
                            contentDescription = "Иконка настроек",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.requiredWidth(6.dp))
                        Text(
                            text = "Настройки",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.requiredHeight(15.dp))
                Row(
                    modifier = modifier
                        .padding(
                            horizontal = 14.dp,
                            vertical = 10.dp
                        )
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = shape
                        )
                        .clip(shape)
                        .padding(DefaultPadding.CardDefaultPaddingSmall),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ChangeChapterButton(
                        modifier = Modifier.weight(1F / 5F).padding(6.dp),
                        icon = Icons.Rounded.ArrowBack,
                        enabled = previousButtonShowed,
                        onClick = openPreviousChapter
                    )
                    Slider(
                        modifier = Modifier.weight(3F / 5F).padding(horizontal = 3.dp),
                        value = pagerState.currentPage.toFloat(),
                        onValueChange = {
                            scope.launch {
                                pagerState.scrollToPage(it.toInt())
                            }
                        },
                        valueRange = (
                                0F..(pagerState.pageCount - 1).coerceAtLeast(1).toFloat()
                                )
                    )
                    ChangeChapterButton(
                        modifier = Modifier.weight(1F / 5F).padding(6.dp),
                        icon = Icons.Rounded.ArrowForward,
                        enabled = nextButtonShowed,
                        onClick = openNextChapter
                    )
                }
            }
        }
    }

    @Suppress("AnimateAsStateLabel")
    @Composable
    private fun ChangeChapterButton(
        modifier: Modifier,
        icon: ImageVector,
        enabled: Boolean,
        onClick: () -> Unit,
    ) {
        val containerColor by animateColorAsState(
            targetValue = if(enabled) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                val primaryContainer = MaterialTheme.colorScheme.primaryContainer.toArgb()
                val surfaceColor = Color.Gray.toArgb()
                val blendedArgb = ColorUtils.blendARGB(primaryContainer, surfaceColor, 0.6f)
                Color(blendedArgb)
            }
        )
        val shape = CardShape.CardStandaloneLarge

        Row(
            modifier = modifier
                .layout { measurable, constraints ->
                    val size = constraints.maxWidth.coerceAtMost(200)
                    val placeable = measurable.measure(
                        constraints.copy(
                            minWidth = size,
                            maxWidth = size,
                            minHeight = size,
                            maxHeight = size
                        )
                    )
                    layout(size, size) {
                        placeable.place(0, 0)
                    }
                }
                .clickable(
                    onClick = onClick,
                    enabled = enabled
                )
                .background(
                    color = containerColor,
                    shape = shape
                )
                .clip(shape),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    @Composable
    private fun ReaderSettingPopup(
        component: SettingsReaderComponent,
        readerSettingsModel: MainReaderComponent.Settings,
        closeDialog: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = closeDialog,
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.8F)

            ) {
                Column(
                    modifier = Modifier
                        .padding(DefaultPadding.CardDefaultPaddingLarge)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Настройки читалки",
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Ночная тема")
                        Switch(
                            checked = readerSettingsModel.nightMode,
                            onCheckedChange = {
                                component.sendIntent(
                                    SettingsReaderComponent.Intent.NightModeChanged(it)
                                )
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Полноэкранный режим")
                        Switch(
                            checked = readerSettingsModel.fullscreenMode,
                            onCheckedChange = {
                                component.sendIntent(
                                    SettingsReaderComponent.Intent.FullscreenModeChanged(it)
                                )
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Размер шрифта",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CustomIconButton(
                            shape = CircleShape,
                            contentColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.background,
                            minSize = 30.dp,
                            onClick = {
                                component.sendIntent(
                                    SettingsReaderComponent.Intent.FontSizeChanged(
                                        (readerSettingsModel.fontSize - 1).coerceAtLeast(1)
                                    )
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = null
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = readerSettingsModel.fontSize.toString())
                        Spacer(modifier = Modifier.width(6.dp))
                        CustomIconButton(
                            shape = CircleShape,
                            contentColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.background,
                            minSize = 30.dp,
                            onClick = {
                                component.sendIntent(
                                    SettingsReaderComponent.Intent.FontSizeChanged(
                                        (readerSettingsModel.fontSize + 1)
                                    )
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowForward,
                                contentDescription = null
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {

                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    Color(readerSettingsModel.lightColor),
                                    RoundedCornerShape(10.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Светлый цвет")
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {

                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    Color(readerSettingsModel.darkColor),
                                    RoundedCornerShape(10.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Тёмный цвет")
                    }
                }
            }
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
        currentIndex += ((get(index).length)/2)
        return currentIndex
    }
}

