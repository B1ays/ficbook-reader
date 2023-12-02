package ru.blays.ficbookReader.components.fanficPage.reader

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.arkivanov.decompose.router.slot.ChildSlot
import com.example.myapplication.compose.Res
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import io.github.skeptick.libres.compose.painterResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.blays.ficbookReader.shared.ui.readerComponents.declaration.MainReaderComponent
import ru.blays.ficbookReader.shared.ui.readerComponents.declaration.SettingsReaderComponent
import ru.blays.ficbookReader.shared.ui.readerComponents.declaration.VoteReaderComponent
import ru.blays.ficbookReader.theme.ReaderTheme
import ru.blays.ficbookReader.values.CardShape
import ru.blays.ficbookReader.values.DefaultPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingsToolbar

@Composable
actual fun FanficReaderContent(component: MainReaderComponent) {
    val state = component.state.subscribeAsState()

    val settingsSlot = component.dialog.subscribeAsState()

    Scaffold(
        topBar = {
            CollapsingsToolbar(
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
                            component.sendIntent(
                                MainReaderComponent.Intent.OpenOrCloseSettings
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
                    titleText = state.value.chapterName
                )
            )
        }
    ) { padding ->
        val reader = remember(state.value.chapterIndex) {
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
        reader.execute(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
                .fillMaxSize(),
            voteComponent = component.voteComponent
        )
    }
}

private class Reader(
    private val settingsSlot: State<ChildSlot<*, SettingsReaderComponent>>,
    private val state: State<MainReaderComponent.State>,
    private val onDispose: (charIndex: Int) -> Unit,
    private val openPreviousChapter: () -> Unit,
    private val openNextChapter: () -> Unit
) {
    var pagerState: PagerState? by mutableStateOf(null)
    var currentCharIndex by mutableStateOf(state.value.initialCharIndex)

    @Composable
    fun execute(
        modifier: Modifier = Modifier,
        voteComponent: VoteReaderComponent
    ) {
        val currentState by state
        val text = currentState.text
        val settings = currentState.settings

        BoxWithConstraints(
            modifier = modifier
        ) {
            val slotInstance = settingsSlot.value.child?.instance
            val twoPanel = maxWidth > 1000.dp
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                ReaderTheme(
                    darkTheme = settings.nightMode,
                    darkColor = Color(settings.darkColor),
                    lightColor = Color(settings.lightColor)
                ) {
                    ReaderContentDesktop(
                        text = text,
                        settings = settings,
                        modifier = Modifier.fillMaxHeight(0.9F),
                        twoPanel = twoPanel,
                        onCenterZoneClick = {},
                        onDispose = onDispose
                    )
                }
                if (pagerState != null) {
                    BottomControl(
                        voteComponent = voteComponent,
                        pagerState = pagerState!!,
                        readerState = currentState,
                        modifier = Modifier.fillMaxHeight(),
                        openNextChapter = openNextChapter,
                        openPreviousChapter = openPreviousChapter
                    )
                }
            }
            AnimatedVisibility(
                visible = slotInstance != null,
                enter = slideInHorizontally(spring()) { -it },
                exit = slideOutHorizontally(spring()) { -it }
            ) {
                val slot = remember { slotInstance!! }
                ReaderSettings(
                    component = slot,
                    modifier = Modifier.width(250.dp)
                )
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun ReaderContentDesktop(
        modifier: Modifier = Modifier,
        text: String,
        twoPanel: Boolean,
        settings: MainReaderComponent.Settings,
        onCenterZoneClick: () -> Unit,
        onDispose: (charIndex: Int) -> Unit
    ) {
        val baseStyle = MaterialTheme.typography.bodyMedium
        val style = remember(settings.fontSize) {
            baseStyle.copy(
                fontSize = settings.fontSize.sp
            )
        }

        val scope = rememberCoroutineScope()

        if (text.isNotEmpty()) {
            BoxWithConstraints(
                modifier = modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(DefaultPadding.CardDefaultPadding)
            ) {
                if (twoPanel) {
                    val density = LocalDensity.current
                    val config = remember(style, constraints.maxHeight, constraints.maxWidth) {
                        TextSplitterConfig.TwoPanelConfig(
                            style = style,
                            constraints = constraints,
                            spaceBetweenPanel = with(density) { 50.dp.roundToPx() }
                        )
                    }
                    val pages = rememberTwoPanelTextPages(
                        text = text,
                        config = config
                    )
                    pagerState = rememberPagerState {
                        pages.size
                    }

                    TwoPanelPager(
                        pagerState = pagerState!!,
                        pages = pages,
                        config = config,
                        onCenterZoneClick = onCenterZoneClick
                    )

                    DisposableEffect(Unit) {
                        val pagerState = pagerState
                        scope.launch {
                            while (pages.isEmpty()) {
                                delay(100)
                            }
                            if (pagerState != null) {
                                val page = pages.findPageIndexForCharIndex(currentCharIndex)
                                pagerState.scrollToPage(page)
                            }
                        }

                        onDispose {
                            pagerState?.let {
                                val absoluteCharIndex = pages.findCharIndexForPageIndex(it.currentPage)
                                currentCharIndex = absoluteCharIndex
                                onDispose(absoluteCharIndex)
                            }
                        }
                    }
                } else {
                    val config = remember(style, constraints.maxHeight, constraints.maxWidth) {
                        TextSplitterConfig.SinglePanelConfig(
                            style = style,
                            constraints = constraints
                        )
                    }
                    val pages = rememberTextPages(
                        text = text,
                        config = config
                    )
                    pagerState = rememberPagerState {
                        pages.size
                    }

                    SinglePanelPager(
                        pagerState = pagerState!!,
                        pages = pages,
                        config = TextSplitterConfig.SinglePanelConfig(
                            style = style,
                            constraints = constraints
                        ),
                        onCenterZoneClick = onCenterZoneClick
                    )

                    DisposableEffect(Unit) {
                        val pagerState = pagerState
                        scope.launch {
                            while (pages.isEmpty()) {
                                delay(100)
                            }
                            if (pagerState != null) {
                                val page = pages.findPageIndexForCharIndex(currentCharIndex)
                                pagerState.scrollToPage(page)
                            }
                        }
                        onDispose {
                            pagerState?.let {
                                val absoluteCharIndex = pages.findCharIndexForPageIndex(it.currentPage)
                                currentCharIndex = absoluteCharIndex
                                onDispose(absoluteCharIndex)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SinglePanelPager(
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
    private fun TwoPanelPager(
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
                val onePanelSize = (constraints.maxWidth - config.spaceBetweenPanel) / 2
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
                    val firstTapZone = 0.0..size.width * (1.0 / 3)
                    val midTapZone = size.width * (1.0 / 3)..size.width * (2.0 / 3)
                    val secondTapZone = size.width * (2.0 / 3)..size.width.toDouble()
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
    private fun BottomControl(
        voteComponent: VoteReaderComponent,
        readerState: MainReaderComponent.State,
        pagerState: PagerState,
        openNextChapter: () -> Unit,
        openPreviousChapter: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val hasPreviousPage = pagerState.canScrollBackward
        val hasNextPage = pagerState.canScrollForward
        val hasNextChapter = readerState.chapterIndex < readerState.chaptersCount - 1
        val hasPreviousChapter = readerState.chapterIndex > 0

        var dialogOpened by remember { mutableStateOf(false) }

        val scope = rememberCoroutineScope()

        if(dialogOpened) {
            val voteState by voteComponent.state.subscribeAsState()
            Dialog(
                onDismissRequest = { dialogOpened = false }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        if(voteState.canVote) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Жду продолжения",
                                    style = MaterialTheme.typography.titleMedium,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    modifier = Modifier.weight(0.6F),
                                )
                                Checkbox(
                                    checked = voteState.votedForContinue,
                                    onCheckedChange = { newValue ->
                                        voteComponent.sendIntent(
                                            VoteReaderComponent.Intent.VoteForContinue(
                                                vote = newValue
                                            )
                                        )
                                    }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Прочитано",
                                style = MaterialTheme.typography.titleMedium,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                modifier = Modifier.weight(0.6F),
                            )
                            Checkbox(
                                checked = voteState.readed,
                                onCheckedChange = { newValue ->
                                    voteComponent.sendIntent(
                                        VoteReaderComponent.Intent.Read(
                                            read = newValue
                                        )
                                    )
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    dialogOpened = false
                                }
                            ) {
                                Text("Ок")
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1F / 5F),
                contentAlignment = Alignment.Center
            ) {
                ChangeChapterButton(
                    visible = !hasPreviousPage && hasPreviousChapter,
                    icon = Icons.Rounded.ArrowBack,
                    contentDescription = "Кнопка предыдущая глава",
                    onClick = openPreviousChapter
                )
            }
            Box(
                modifier = Modifier.weight(3F / 5F)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1}/${pagerState.pageCount}"
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
                            0F..(pagerState.pageCount - 1).coerceAtLeast(1).toFloat()
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${pagerState.currentPage + 1 percentageOf pagerState.pageCount}%"
                    )
                }
            }
            Box(
                modifier = Modifier.weight(1F / 5F),
                contentAlignment = Alignment.Center
            ) {
                ChangeChapterButton(
                    visible = !hasNextPage && hasNextChapter,
                    icon = Icons.Rounded.ArrowForward,
                    contentDescription = "Кнопка следующая глава",
                    onClick = openNextChapter
                )
            }
        }

        LaunchedEffect(hasNextPage, hasNextChapter) {
            if(!hasNextPage && !hasNextChapter && pagerState.pageCount >= 1) {
                dialogOpened = true
            }
        }
    }

    @Composable
    private fun ChangeChapterButton(
        visible: Boolean,
        icon: ImageVector,
        contentDescription: String? = null,
        onClick: () -> Unit
    ) {
        AnimatedContent(
            targetState = visible,
            modifier = Modifier.layout { measurable, _ ->
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
                        .place(0, -(maxSize / 2))
                }
            }
        ) { state ->
            if (state) {
                val shape = CardShape.CardStandaloneLarge
                Row(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = shape
                        )
                        .clip(shape)
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
    private fun ReaderSettings(
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
            val darkColor = remember(state.darkColor) { Color(state.darkColor) }
            var darkColorSelectorOpen by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            color = darkColor,
                            shape = CardDefaults.shape
                        )
                        .clip(CardDefaults.shape)
                        .clickable {
                            darkColorSelectorOpen = !darkColorSelectorOpen
                        }
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Тёмный цвет")
            }
            ColorPickerItem(
                visible = darkColorSelectorOpen,
                initialColor = darkColor,
                onColorSelected = { colorArgb ->
                    darkColorSelectorOpen = false
                    component.sendIntent(
                        SettingsReaderComponent.Intent.DarkColorChanged(colorArgb)
                    )
                }
            )
            Spacer(modifier = Modifier.height(6.dp))
            val lightColor = remember(state.lightColor) { Color(state.lightColor) }
            var lightColorSelectorOpen by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            color = lightColor,
                            shape = CardDefaults.shape
                        )
                        .clip(CardDefaults.shape)
                        .clickable {
                            lightColorSelectorOpen = !lightColorSelectorOpen
                        }
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Светлый цвет")
            }
            ColorPickerItem(
                visible = lightColorSelectorOpen,
                initialColor = lightColor,
                onColorSelected = { colorArgb ->
                    lightColorSelectorOpen = false
                    component.sendIntent(
                        SettingsReaderComponent.Intent.LightColorChanged(colorArgb)
                    )
                }
            )
            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Размер шрифта",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            FlowRow {
                for (size in 8..18) {
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
                        .padding(horizontal = 5.dp)
                        .height(200.dp)
                        .fillMaxWidth(),
                ) { hsvColor ->
                    newColor = hsvColor
                }
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = {
                        onColorSelected(newColor.toColor().toArgb())
                    }
                ) {
                    Text("Выбрать")
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        painter = painterResource(Res.image.ic_dropper),
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
        for (i in indices) {
            val pageLength = get(i).length
            if(index >= currentIndex && index < currentIndex + pageLength) {
                return i
            }
            currentIndex += get(i).length
        }
        return 0
    }

    @JvmName("findPageIndexForCharIndexTwoPanel")
    private fun List<Pair<String, String>>.findPageIndexForCharIndex(index: Int): Int {
        var currentIndex = 0
        for ((i, e) in this.withIndex()) {
            val pageLength = with(e) { first.length + second.length }
            if (index >= currentIndex && index < currentIndex + pageLength) {
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

    @JvmName("findCharIndexForPageIndexTwoPanel")
    private fun List<Pair<String, String>>.findCharIndexForPageIndex(index: Int): Int {
        var currentIndex = 0
        for (i in 0 until index) {
            currentIndex += with(get(i)) { first.length + second.length }
        }
        currentIndex += get(index).first.length
        return currentIndex
    }
}