package ru.blays.ficbookReader.components.fanficPage

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pullrefresh.PullRefreshIndicator
import androidx.compose.material3.pullrefresh.pullRefresh
import androidx.compose.material3.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.example.myapplication.compose.Res
import com.moriatsushi.insetsx.systemBarsPadding
import kotlinx.coroutines.launch
import ru.blays.ficbookReader.platformUtils.BackHandler
import ru.blays.ficbookReader.platformUtils.WindowSize
import ru.blays.ficbookReader.platformUtils.blurPlatform
import ru.blays.ficbookReader.platformUtils.blurSupported
import ru.blays.ficbookReader.shared.data.dto.*
import ru.blays.ficbookReader.shared.platformUtils.shareSupported
import ru.blays.ficbookReader.shared.ui.fanficPageComponents.declaration.FanficPageInfoComponent
import ru.blays.ficbookReader.theme.trophyColor
import ru.blays.ficbookReader.ui_components.CustomBottomSheetScaffold.BottomSheetScaffold
import ru.blays.ficbookReader.ui_components.CustomBottomSheetScaffold.SheetValue
import ru.blays.ficbookReader.ui_components.FanficComponents.CircleChip
import ru.blays.ficbookReader.ui_components.FanficComponents.FanficTagChip
import ru.blays.ficbookReader.ui_components.LinkifyText.TextWithLinks
import ru.blays.ficbookReader.ui_components.Scrollbar.VerticalScrollbar
import ru.blays.ficbookReader.values.CardShape
import ru.blays.ficbookReader.values.DefaultPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingsToolbar
import ru.hh.toolbar.custom_toolbar.rememberToolbarScrollBehavior

@Composable
fun FanficPageInfoContent(component: FanficPageInfoComponent) {
    val windowSize = WindowSize()

    if(windowSize.width > 600) {
        LandscapeContent(component)
    } else {
        PortraitContent(component)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortraitContent(component: FanficPageInfoComponent) {
    val state by component.state.subscribeAsState()
    val fanfic = remember(state) { state.fanfic }
    val isLoading = remember(state) { state.isLoading }

    val bottomSheetState =
        ru.blays.ficbookReader.ui_components.CustomBottomSheetScaffold.rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )
    val snackbarHostState = remember { SnackbarHostState() }
    val bottomSheetScaffoldState =
        ru.blays.ficbookReader.ui_components.CustomBottomSheetScaffold.rememberBottomSheetScaffoldState(
            bottomSheetState = bottomSheetState,
            snackbarHostState = snackbarHostState
        )
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = {
            component.sendIntent(
                FanficPageInfoComponent.Intent.Refresh
            )
        }
    )
    val scrollBehavior = rememberToolbarScrollBehavior()

    val scope = rememberCoroutineScope()

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        if(fanfic != null && !isLoading) {
            val coverPainter: AsyncImagePainter? = if(fanfic.coverUrl.isNotEmpty()) {
                rememberAsyncImagePainter(fanfic.coverUrl)
            } else {
                null
            }
            if (coverPainter != null && blurSupported) {
                val bounds = with(LocalDensity.current) {
                    Rect(
                        Offset(0f, 0f),
                        Offset(maxWidth.toPx(), maxHeight.toPx()),
                    )
                }
                Image(
                    painter = coverPainter,
                    contentDescription = "Обложка фанфика",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blurPlatform(
                            areas = arrayOf(bounds),
                            backgroundColor = MaterialTheme.colorScheme.background,
                            tint = MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.4F
                            ),
                            blurRadius = 14.dp,
                            noiseFactor = 0.08F
                        )
                )
            }
            val sheetProgress = remember(bottomSheetState.offset) {
                bottomSheetState.progressBetweenTwoValues(
                    SheetValue.Expanded,
                    SheetValue.PartiallyExpanded
                )
            }
            val sheetBackground = MaterialTheme.colorScheme.surfaceVariant
            val sheetBackgroundAtProgress = remember(sheetProgress) {
                sheetBackground.copy(
                    alpha = sheetProgress
                )
            }
            Box(
                modifier = Modifier.systemBarsPadding()
            ) {
                BottomSheetScaffold(
                    scaffoldState = bottomSheetScaffoldState,
                    sheetPeekHeight = 110.dp,
                    containerColor = Color.Transparent,
                    sheetContainerColor = sheetBackgroundAtProgress,
                    sheetShadowElevation = 0.dp,
                    sheetSwipeEnabled = fanfic.chapters !is FanficChapterStable.SingleChapterModel,
                    sheetContent = {
                        BackHandler(true) {
                            when (bottomSheetState.currentValue) {
                                SheetValue.Expanded -> {
                                    scope.launch {
                                        bottomSheetScaffoldState.bottomSheetState.partialExpand()
                                    }
                                }
                                else -> {
                                    component.onOutput(
                                        FanficPageInfoComponent.Output.ClosePage
                                    )
                                }
                            }
                        }
                        BottomSheetContentClosed(
                            component = component,
                            fanficPage = fanfic,
                            sheetProgress = sheetProgress
                        ) {
                            component.onOutput(
                                FanficPageInfoComponent.Output.OpenLastOrFirstChapter(
                                    fanficID = fanfic.fanficID,
                                    chapter = fanfic.chapters
                                )
                            )
                        }
                        Spacer(
                            modifier = Modifier.height(
                                height = (24*(1-sheetProgress)).dp
                            )
                        )
                        val chapters = fanfic.chapters
                        if(chapters is FanficChapterStable.SeparateChaptersModel) {
                            BottomSheetContentOpened(
                                reversed = state.reverseOrderEnabled,
                                chapters = chapters,
                                onChapterClicked = { index ->
                                    component.onOutput(
                                        FanficPageInfoComponent.Output.OpenChapter(
                                            fanficID = fanfic.fanficID,
                                            index = index,
                                            chapters = fanfic.chapters
                                        )
                                    )
                                },
                                onCommentClicked = { chapterID ->
                                    component.onOutput(
                                        FanficPageInfoComponent.Output.OpenPartComments(chapterID = chapterID)
                                    )
                                }
                            )
                        }
                    },
                    topBar = {
                        CollapsingsToolbar(
                            scrollBehavior = scrollBehavior,
                            containerColor = Color.Transparent,
                            collapsedElevation = 0.dp,
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        component.onOutput(
                                            FanficPageInfoComponent.Output.ClosePage
                                        )
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(Res.image.ic_arrow_back),
                                        contentDescription = "Стрелка назад"
                                    )
                                }
                            },
                            actions = {
                                topBarActions(component)
                            },
                            collapsingTitle = CollapsingTitle.small(fanfic.name)
                        )
                    }
                ) { padding ->
                    FanficDescription(
                        component = component,
                        fanfic = fanfic,
                        coverPainter = coverPainter,
                        modifier = Modifier
                            .padding(
                                top = padding.calculateTopPadding(),
                                bottom = 110.dp
                            )
                            .pullRefresh(state = pullRefreshState)
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                    )
                }
            }
        }
        PullRefreshIndicator(
            refreshing = isLoading,
            state = pullRefreshState,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun LandscapeContent(
    component: FanficPageInfoComponent
) {
    val state by component.state.subscribeAsState()
    val fanfic = state.fanfic
    val isLoading = state.isLoading

    if(fanfic != null && !isLoading) {
        val coverPainter: AsyncImagePainter? = if(fanfic.coverUrl.isNotEmpty()) {
            rememberAsyncImagePainter(fanfic.coverUrl)
        } else {
            null
        }
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            ModalDrawerSheet(
                modifier = Modifier
                    .systemBarsPadding()
                    .fillMaxWidth(0.35F)
            ) {
                BottomSheetContentClosed(
                    component = component,
                    fanficPage = fanfic,
                    sheetProgress = 0F
                ) {
                    component.onOutput(
                        FanficPageInfoComponent.Output.OpenLastOrFirstChapter(
                            fanficID = fanfic.fanficID,
                            chapter = fanfic.chapters
                        )
                    )
                }
                val chapters = fanfic.chapters
                if(chapters is FanficChapterStable.SeparateChaptersModel) {
                    BottomSheetContentOpened(
                        reversed = state.reverseOrderEnabled,
                        modifier = Modifier.padding(end = 4.dp),
                        chapters = chapters,
                        onCommentClicked = { chapterID ->
                            component.onOutput(
                                FanficPageInfoComponent.Output.OpenPartComments(chapterID = chapterID)
                            )
                        }
                    ) { index ->
                        component.onOutput(
                            FanficPageInfoComponent.Output.OpenChapter(
                                fanficID = fanfic.fanficID,
                                index = index,
                                chapters = fanfic.chapters
                            )
                        )
                    }
                }
            }
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                if (coverPainter != null && blurSupported) {
                    val bounds = with(LocalDensity.current) {
                        Rect(
                            Offset(0f, 0f),
                            Offset(maxWidth.toPx(), maxHeight.toPx()),
                        )
                    }
                    Image(
                        painter = coverPainter,
                        contentDescription = "Обложка фанфика",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxSize()
                            .blurPlatform(
                                arrayOf(bounds),
                                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                                tint = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.4F
                                ),
                                blurRadius = 12.dp,
                                noiseFactor = 0.08F
                            )
                    )
                }
                Scaffold(
                    modifier = Modifier.systemBarsPadding(),
                    topBar = {
                        CollapsingsToolbar(
                            containerColor = Color.Transparent,
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        component.onOutput(
                                            FanficPageInfoComponent.Output.ClosePage
                                        )
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(Res.image.ic_arrow_back),
                                        contentDescription = "Стрелка назад"
                                    )
                                }
                            },
                            actions = {
                                topBarActions(component)
                            },
                            collapsingTitle = CollapsingTitle.small(fanfic.name)
                        )
                    },
                    containerColor = Color.Transparent
                ) { padding ->
                    FanficDescription(
                        component = component,
                        fanfic = fanfic,
                        coverPainter = coverPainter,
                        modifier = Modifier.padding(
                            top = padding.calculateTopPadding()
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FanficHeader(
    fanficPage: FanficPageModelStable,
    coverPainter: AsyncImagePainter?,
    onAuthorClick: (author: UserModelStable) -> Unit
) {
    FlowRow {
        fanficPage.authors.forEach { userModel ->
            AuthorItem(
                authorModel = userModel,
                avatarSize = 40.dp,
                onAuthorClick = onAuthorClick
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))

    if(coverPainter != null) {
        var isCoverExpanded by remember {
            mutableStateOf(false)
        }
        val animatedCoverWidth by animateFloatAsState(
            targetValue = if (isCoverExpanded) 1F else 0.4F,
            animationSpec = spring()
        )

        Image(
            painter = coverPainter,
            contentDescription = "Обложка фанфика",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .layout { measurable, constraints ->
                    val fullWidth = constraints.maxWidth
                    val coverWidth = (fullWidth * animatedCoverWidth).toInt()
                    val coverHeight = (coverWidth * 1.5F).toInt()
                    val coverPlaceX = ((fullWidth - coverWidth) / 2F).toInt()

                    layout(
                        width = constraints.maxWidth,
                        height = coverHeight
                    ) {
                        val placeable = measurable.measure(
                            constraints.copy(
                                minWidth = coverWidth,
                                maxWidth = coverWidth,
                                minHeight = coverHeight,
                                maxHeight = coverHeight
                            )
                        )
                        placeable.placeRelative(
                            x = coverPlaceX,
                            y = 0
                        )
                    }
                }
                .clip(CardShape.CardStandalone)
                .shadow(
                    elevation = 4.dp,
                    shape = CardShape.CardStandalone,
                    clip = true
                )
                .clickable {
                    isCoverExpanded = !isCoverExpanded
                }
        )
    }
}

@Composable
private fun FanficDescription(
    component: FanficPageInfoComponent,
    fanfic: FanficPageModelStable,
    coverPainter: AsyncImagePainter?,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = modifier
                .align(Alignment.TopStart)
                .padding(DefaultPadding.CardDefaultPadding)
                .padding(end = 4.dp)
                .fillMaxSize(),
            state = lazyListState
        ) {
            item {
                FanficHeader(
                    fanficPage = fanfic,
                    coverPainter = coverPainter
                ) { author ->
                    component.onOutput(
                        FanficPageInfoComponent.Output.OpenAuthor(
                            href = author.href
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                FanficActionsContent(component.actionsComponent)
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                FanficTags(
                    modifier = Modifier.fillMaxWidth(),
                    tags = fanfic.tags
                ) { tag ->
                    component.onOutput(
                        FanficPageInfoComponent.Output.OpenSection(
                            name = tag.name,
                            href = tag.href
                        )
                    )
                }
            }
            item {
                Fandoms(
                    fandoms = fanfic.fandoms
                ) { fandom ->
                    component.onOutput(
                        FanficPageInfoComponent.Output.OpenSection(
                            name = fandom.name,
                            href = fandom.href
                        )
                    )
                }
            }
            item {
                Pairings(
                    pairings = fanfic.pairings,
                    onPairingClick = { pairing ->
                        component.onOutput(
                            FanficPageInfoComponent.Output.OpenSection(
                                name = pairing.character,
                                href = pairing.href
                            )
                        )
                    }
                )
            }
            item {
                Text(
                    text = "Описание:",
                    style = MaterialTheme.typography.titleMedium
                )
                TextWithLinks(
                    text = fanfic.description,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    onUrlClick = { url ->
                        component.onOutput(
                            FanficPageInfoComponent.Output.OpenUrl(url)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(fanfic.rewards) {reward ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier
                            .padding(3.dp)
                            .size(18.dp),
                        painter = painterResource(Res.image.ic_trophy),
                        contentDescription = "Значок награды",
                        tint = trophyColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "«${reward.message}» от ${reward.fromUser}",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = reward.awardDate,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
        VerticalScrollbar(
            lazyListState = lazyListState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(end = 3.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Fandoms(
    fandoms: List<FandomModelStable>,
    onFandomClick: (fandom: FandomModelStable) -> Unit
) {
    FlowRow {
        Text(
            text = "Фэндомы:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.requiredWidth(3.dp))
        fandoms.forEach { fandom ->
            Text(
                text = fandom.name,
                style = MaterialTheme.typography.labelLarge,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(2.dp)
                    .clickable {
                        onFandomClick(fandom)
                    }
            )
        }

    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Pairings(
    pairings: List<PairingModelStable>,
    onPairingClick: (pairing: PairingModelStable) -> Unit
) {
    FlowRow {
        val shape = remember { RoundedCornerShape(percent = 20) }
        Text(
            text = "Пэйринги и персонажи:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.requiredWidth(3.dp))
        pairings.forEach { pairing ->
            Text(
                text = pairing.character + ',',
                style = MaterialTheme.typography.labelLarge,
                color = if (pairing.isHighlighted) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    Color.Unspecified
                },
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(2.dp)
                    .background(
                        color = if (pairing.isHighlighted) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.Unspecified
                        },
                        shape = shape
                    )
                    .clip(shape)
                    .clickable {
                        onPairingClick(pairing)
                    }
            )
            Spacer(modifier = Modifier.requiredWidth(3.dp))
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FanficTags(
    modifier: Modifier = Modifier,
    tags: List<FanficTagStable>,
    onTagClick: (tag: FanficTagStable) -> Unit
) {
    FlowRow(
        modifier = modifier
    ) {
        tags.forEach { tag ->
            FanficTagChip(
                tag = tag,
                onClick = {
                    onTagClick(tag)
                }
            )
        }
    }
}

@Composable
private fun BottomSheetContentClosed(
    component: FanficPageInfoComponent,
    fanficPage: FanficPageModelStable,
    sheetProgress: Float = 1F,
    onReadClicked: () -> Unit
) {
    val state by component.state.subscribeAsState()
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .padding(DefaultPadding.CardDefaultPadding)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Глав: ${fanficPage.chapters.size}",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Страниц: ${fanficPage.pagesCount}",
                style = MaterialTheme.typography.titleMedium
            )
        }

        if(sheetProgress < 1F) {
            Button(
                onClick = onReadClicked,
                modifier = Modifier.graphicsLayer(
                    alpha = 1-sheetProgress
                )
            ) {
                Icon(
                    modifier = Modifier.size(14.dp),
                    painter = painterResource(Res.image.ic_open_book),
                    contentDescription = "Иконка книги"
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Читать"
                )
            }
        } else {
            IconButton(
                onClick = { menuExpanded = !menuExpanded }
            ) {
                Icon(
                    painter = painterResource(Res.image.ic_more_vertical),
                    contentDescription = "Меню",
                    modifier = Modifier.size(20.dp)
                )
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },

                    ) {
                    DropdownMenuItem(
                        text = {
                            Text("Обратный порядок глав")
                        },
                        trailingIcon = {
                            Checkbox(
                                checked = state.reverseOrderEnabled,
                                onCheckedChange = {
                                    component.sendIntent(
                                        FanficPageInfoComponent.Intent.ChangeChaptersOrder(it)
                                    )
                                }
                            )
                        },
                        onClick = {
                            component.sendIntent(
                                FanficPageInfoComponent.Intent.ChangeChaptersOrder(!state.reverseOrderEnabled)
                            )
                        }
                    )
                }
            }

        }
    }
    HorizontalDivider(
        modifier = Modifier
            .padding(vertical = 2.dp)
            .graphicsLayer(
            scaleY = sheetProgress
        ),
        color = MaterialTheme.colorScheme.outline,
        thickness = (1.5F).dp
    )
}

@Composable
private
fun BottomSheetContentOpened(
    reversed: Boolean,
    modifier: Modifier = Modifier,
    chapters: FanficChapterStable.SeparateChaptersModel,
    onCommentClicked: (chapterID: String) -> Unit,
    onChapterClicked: (index: Int) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val reversedList = remember(reversed) {
        if(reversed) {
            chapters.chapters.reversed()
        } else {
            chapters.chapters
        }
    }
    Box {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            state = lazyListState
        ) {
            itemsIndexed(reversedList) { index, item ->
                ChapterItem(
                    index = if(reversed) reversedList.size - index else index+1,
                    chapter = item,
                    isReaded = item.readed,
                    onCommentsClicked = {
                        onCommentClicked(item.chapterID)
                    },
                    onClick = {
                        onChapterClicked(index)
                    }
                )
            }
        }
        VerticalScrollbar(
            lazyListState = lazyListState,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 3.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterItem(
    index: Int,
    chapter: FanficChapterStable.SeparateChaptersModel.Chapter,
    isReaded: Boolean,
    onCommentsClicked: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(DefaultPadding.CardDefaultPaddingLarge)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(0.7F),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = if (isReaded) Color.Transparent else
                                MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .then(
                            if (isReaded) Modifier.border(
                                2.dp,
                                MaterialTheme.colorScheme.outline,
                                CircleShape
                            ) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = index.toString(),
                        color = if(isReaded) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = chapter.name,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = chapter.date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            CircleChip(
                color = MaterialTheme.colorScheme.primaryContainer, //TODO "surfaceContainerLowest"
                modifier = Modifier.clickable(onClick = onCommentsClicked)
            ) {
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    painter = painterResource(Res.image.ic_comment),
                    contentDescription = "Иконка комментария",
                    modifier = Modifier
                        .padding(4.dp)
                        .size(18.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(text = chapter.commentsCount.toString())
                Spacer(modifier = Modifier.width(2.dp))
            }
        }
    }
}

@Composable
private fun AuthorItem(
    authorModel: FanficAuthorModelStable,
    avatarSize: Dp,
    onAuthorClick: (author: UserModelStable) -> Unit
) {
    val user = authorModel.user
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(5.dp)
            .clickable {
                onAuthorClick(user)
            }
    ) {
        AsyncImage(
            model = user.avatarUrl,
            contentDescription = "Аватар автора",
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Column {
            Text(
                text = user.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = authorModel.role,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.surfaceTint
            )
        }
    }
}

val topBarActions: @Composable RowScope.(component: FanficPageInfoComponent) -> Unit = { component ->
    var dropDownMenuState by remember { mutableStateOf(false) }
    IconButton(
        onClick = {
            dropDownMenuState = !dropDownMenuState
        }
    ) {
        Icon(
            painter = painterResource(Res.image.ic_more_vertical),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
    }
    DropdownMenu(
        expanded = dropDownMenuState,
        onDismissRequest = {
            dropDownMenuState = false
        }
    ) {
        if(shareSupported) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Поделиться",
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.image.ic_share),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                onClick = {
                    dropDownMenuState = false
                    component.sendIntent(FanficPageInfoComponent.Intent.Share)
                }
            )
        }
        DropdownMenuItem(
            text = {
                Text(
                    text = "Копировать ccылку",
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.image.ic_link),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            onClick = {
                dropDownMenuState = false
                component.sendIntent(FanficPageInfoComponent.Intent.CopyLink)
            }
        )
        DropdownMenuItem(
            text = {
                Text(
                    text = "Открыть в браузере",
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.image.ic_globe),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            onClick = {
                dropDownMenuState = false
                component.sendIntent(FanficPageInfoComponent.Intent.OpenInBrowser)
            }
        )
    }
}