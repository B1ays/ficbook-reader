package ru.blays.ficbookReader.components.fanficPage.reader

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.arkivanov.decompose.router.slot.ChildSlot
import com.example.myapplication.compose.Res
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import com.godaddy.android.colorpicker.toColorInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.blays.ficbookReader.platformUtils.FullscreenContainer
import ru.blays.ficbookReader.platformUtils.rememberTimeObserver
import ru.blays.ficbookReader.shared.ui.readerComponents.declaration.MainReaderComponent
import ru.blays.ficbookReader.shared.ui.readerComponents.declaration.SettingsReaderComponent
import ru.blays.ficbookReader.shared.ui.readerComponents.declaration.VoteReaderComponent
import ru.blays.ficbookReader.theme.ReaderTheme
import ru.blays.ficbookReader.ui_components.CustomButton.CustomIconButton
import ru.blays.ficbookReader.ui_components.FanficComponents.CircleChip
import ru.blays.ficbookReader.ui_components.pager2.*
import ru.blays.ficbookReader.values.CardShape
import ru.blays.ficbookReader.values.DefaultPadding

@Composable
actual fun FanficReaderContent(component: MainReaderComponent) {
    val state = component.state.subscribeAsState()
    val settingsSlot = component.dialog.subscribeAsState()
    val controlEventSource = remember { MutableStateFlow(false) }

    val reader = remember {
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
            },
            openSettings = {
                component.sendIntent(
                    MainReaderComponent.Intent.OpenOrCloseSettings
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

private class Reader(
    private val settingsSlot: State<ChildSlot<*, SettingsReaderComponent>>,
    private val state: State<MainReaderComponent.State>,
    private val onDispose: (charIndex: Int) -> Unit,
    private val onCenterZoneClick: () -> Unit,
    private val openPreviousChapter: () -> Unit,
    private val openNextChapter: () -> Unit,
    private val openSettings: () -> Unit
) {
    val pagerState: PagerState2<String> = PagerStateImpl2(
        initialPages = emptyList(),
        initialPageIndex = 0,
        initialPageOffsetFraction = 0f
    )
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
                    Control(
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
                    modifier = Modifier.weight(0.04F)
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
        val textMeasurer = rememberTextMeasurer()
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

            DisposableEffect(text) {
                val job = scope.launch {
                    val pages = splitTextToPages(
                        text = text,
                        config = config,
                        textMeasurer = textMeasurer
                    )
                    pagerState.updatePages { pages }
                }
                onDispose {
                    job.cancel()
                }
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
                                    pagerState.animatedScrollToNextPage()
                                }
                                true
                            }
                            VOLUME_DOWN -> {
                                scope.launch {
                                    pagerState.animatedScrollToPreviousPage()
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

            DisposableEffect(Unit) {
                scope.launch {
                    val pages = pagerState.pages
                    while(pagerState.pageCount == 0) {
                        delay(100)
                    }
                    val page = pages.findPageIndexForCharIndex(currentCharIndex)
                    pagerState.scrollToPage(page.coerceAtMost(pages.lastIndex))
                }

                onDispose {
                    val pages = pagerState.pages
                    val absoluteCharIndex = pages.findCharIndexForPageIndex(pagerState.currentPage)
                    currentCharIndex = absoluteCharIndex
                    onDispose(absoluteCharIndex)
                }
            }
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
                                    pagerState.scrollToPreviousPage()
                                }
                            }
                            in midTapZone -> onCenterZoneClick()
                            in secondTapZone -> {
                                scope.launch {
                                    pagerState.scrollToNextPage()
                                }
                            }
                        }
                    }
                },
            pageContent = pagerContent
        )
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

    @Suppress("AnimateAsStateLabel")
    @Composable
    private fun Control(
        voteComponent: VoteReaderComponent,
        pagerState: PagerState,
        readerState: MainReaderComponent.State,
        eventSource: Flow<Boolean>,
        modifier: Modifier = Modifier,
        openNextChapter: () -> Unit,
        openPreviousChapter: () -> Unit,
        openSettings: () -> Unit
    ) {
        val voteState by voteComponent.state.subscribeAsState()

        val hasPreviousPage = pagerState.canScrollBackward
        val hasNextPage = pagerState.canScrollForward
        val hasNextChapter = readerState.chapterIndex < readerState.chaptersCount-1
        val hasPreviousChapter = readerState.chapterIndex > 0

        val previousButtonActive = hasPreviousChapter && !hasPreviousPage
        val nextButtonActive = hasNextChapter && !hasNextPage

        val scope = rememberCoroutineScope()

        var expanded by remember { mutableStateOf(false) }

        val shape = CardDefaults.shape

        LaunchedEffect(previousButtonActive, nextButtonActive, hasNextPage) {
            if(previousButtonActive || nextButtonActive) {
                expanded = true
            }
            if(!hasNextChapter && !hasNextPage) {
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
                AnimatedVisibility(
                    visible = !hasNextChapter && !hasNextPage,
                    enter = fadeIn(spring()),
                    exit = fadeOut(spring()),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if(voteState.canVote) {
                            val backgroundColor by animateColorAsState(
                                targetValue = if(voteState.votedForContinue) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                animationSpec = spring()
                            )
                            val contentColor by animateColorAsState(
                                targetValue = if(voteState.votedForContinue) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                            CircleChip(
                                color = backgroundColor,
                                modifier = Modifier.toggleable(
                                    value = voteState.votedForContinue,
                                    onValueChange = { newValue ->
                                        voteComponent.sendIntent(
                                            VoteReaderComponent.Intent.VoteForContinue(
                                                vote = newValue
                                            )
                                        )
                                    }
                                )
                            ) {
                                Icon(
                                    painter = painterResource(Res.image.ic_clock),
                                    contentDescription = "Проголосовать за продолжение",
                                    tint = contentColor,
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Жду продолжения",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = contentColor
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                            }
                        }
                        val backgroundColor by animateColorAsState(
                            targetValue = if(voteState.readed) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            animationSpec = spring()
                        )
                        val contentColor by animateColorAsState(
                            targetValue = if(voteState.readed) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                        CircleChip(
                            color = backgroundColor,
                            modifier = Modifier.toggleable(
                                value = voteState.readed,
                                onValueChange = { newValue ->
                                    voteComponent.sendIntent(
                                        VoteReaderComponent.Intent.Read(
                                            read = newValue
                                        )
                                    )
                                }
                            )
                        ) {
                            Icon(
                                painter = painterResource(Res.image.ic_book),
                                contentDescription = "Прочитанно",
                                tint = contentColor,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Прочитано",
                                style = MaterialTheme.typography.labelLarge,
                                color = contentColor
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.requiredHeight(8.dp))
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
                        enabled = previousButtonActive,
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
                        valueRange = 0F..(pagerState.pageCount - 1)
                            .coerceAtLeast(1)
                            .toFloat()
                    )
                    ChangeChapterButton(
                        modifier = Modifier.weight(1F / 5F).padding(6.dp),
                        icon = Icons.Rounded.ArrowForward,
                        enabled = nextButtonActive,
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
        val scrollState = rememberScrollState()
        AlertDialog(
            onDismissRequest = closeDialog,
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(0.8F)
            ) {
                Column(
                    modifier = Modifier
                        .padding(DefaultPadding.CardDefaultPaddingLarge)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
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
                        Text(
                            text = "Ночная тема",
                            modifier = Modifier.fillMaxWidth(0.7F)
                        )
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
                        Text(
                            text = "Полноэкранный режим",
                            modifier = Modifier.fillMaxWidth(0.7F)
                        )
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Перелистывание кнопками громкости",
                            modifier = Modifier.fillMaxWidth(0.7F)
                        )
                        Switch(
                            checked = readerSettingsModel.scrollWithVolumeButtons,
                            onCheckedChange = {
                                component.sendIntent(
                                    SettingsReaderComponent.Intent.ScrollWithVolumeKeysChanged(it)
                                )
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Не выключать экран",
                            modifier = Modifier.fillMaxWidth(0.7F)
                        )
                        Switch(
                            checked = readerSettingsModel.keepScreenOn,
                            onCheckedChange = {
                                component.sendIntent(
                                    SettingsReaderComponent.Intent.KeepScreenOnChanged(it)
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
                    val lightColor = remember(readerSettingsModel.lightColor) { Color(readerSettingsModel.lightColor) }
                    var lightColorSelectorOpen by remember { mutableStateOf(false) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            lightColorSelectorOpen = !lightColorSelectorOpen
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    color = lightColor,
                                    shape = RoundedCornerShape(10.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Светлый цвет")
                    }
                    ColorPickerItem(
                        visible = lightColorSelectorOpen,
                        initialColor = lightColor,
                        onColorSelected = { colorArgb ->
                            component.sendIntent(
                                SettingsReaderComponent.Intent.LightColorChanged(colorArgb)
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    var darkColorSelectorOpen by remember { mutableStateOf(false) }
                    val darkColor = remember(readerSettingsModel.darkColor) { Color(readerSettingsModel.darkColor) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            darkColorSelectorOpen = !darkColorSelectorOpen
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    darkColor,
                                    RoundedCornerShape(10.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Тёмный цвет")
                    }
                    ColorPickerItem(
                        visible = darkColorSelectorOpen,
                        initialColor = darkColor,
                        onColorSelected = { colorArgb ->
                            component.sendIntent(
                                SettingsReaderComponent.Intent.DarkColorChanged(colorArgb)
                            )
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun ColorPickerItem(
        visible: Boolean,
        initialColor: Color,
        onColorSelected: (colorArgb: Int) -> Unit
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            var newColor by remember { mutableStateOf(HsvColor.from(initialColor)) }
            Column {
                Spacer(modifier = Modifier.height(10.dp))
                ClassicColorPicker(
                    color = newColor,
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth(),
                ) { hsvColor ->
                    newColor = hsvColor
                }
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = {
                        onColorSelected(newColor.toColorInt())
                    }
                ) {
                    Text("Выбрать")
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        painter = painterResource(id = Res.image.ic_dropper),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
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