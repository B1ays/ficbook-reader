package ru.blays.ficbookReader.components.fanficPage

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.pullrefresh.PullRefreshIndicator
import androidx.compose.material3.pullrefresh.pullRefresh
import androidx.compose.material3.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.example.myapplication.compose.Res
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch
import ru.blays.ficbookReader.platformUtils.BackHandler
import ru.blays.ficbookReader.platformUtils.WindowSize
import ru.blays.ficbookReader.shared.data.dto.FanficChapterStable
import ru.blays.ficbookReader.shared.data.dto.FanficPageModelStable
import ru.blays.ficbookReader.shared.data.dto.FanficTagStable
import ru.blays.ficbookReader.shared.data.dto.PairingModelStable
import ru.blays.ficbookReader.shared.ui.fanficPageComponents.declaration.FanficPageInfoComponent
import ru.blays.ficbookReader.theme.likeColor
import ru.blays.ficbookReader.theme.trophyColor
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FanficHeader(fanficPage: FanficPageModelStable) {
    val status = fanficPage.status

    var isCoverExpanded by remember {
        mutableStateOf(false)
    }
    val animatedCoverWidth by animateFloatAsState(
    targetValue = if (isCoverExpanded) 1F else 0.35F,
    animationSpec = spring()
    )

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(fanficPage.coverUrl.isNotEmpty()) {
            KamelImage(
                modifier = Modifier
                    .layout { measurable, constraints ->
                        val width = constraints.maxWidth * animatedCoverWidth
                        val height = width * 1.5F

                        layout(width = width.toInt(), height = height.toInt()) {
                            val placeable = measurable.measure(
                                Constraints(
                                    maxHeight = height.toInt(),
                                    maxWidth = width.toInt()
                                )
                            )
                            placeable.place(0, 0)
                        }
                    }
                    .clip(CardShape.CardStandalone)
                    .clickable {
                        isCoverExpanded = !isCoverExpanded
                    },
                resource = asyncPainterResource(data = fanficPage.coverUrl) ,
                contentDescription = "Обложка фанфика",
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Spacer(modifier = Modifier.width(7.dp))
        Column(
            modifier = Modifier
        ) {
            FlowRow(
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(Res.image.ic_user),
                    contentDescription = "Иконка человек"
                )
                Spacer(modifier = Modifier.width(4.dp))
                fanficPage.author.forEach { author ->
                    Text(
                        text = author.name + ',',
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                }

            }
            Spacer(modifier = Modifier.height(7.dp))
            FlowRow(
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(Res.image.ic_open_book),
                    contentDescription = "Иконка открытая книга"
                )
                Spacer(modifier = Modifier.width(4.dp))
                fanficPage.fandoms.forEach { fandom ->
                    Text(
                        text = fandom.name + ',',
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                }
            }
            Spacer(modifier = Modifier.height(7.dp))
            Row {
                if (status.likes != 0) {
                    CircleChip(
                        color = MaterialTheme.colorScheme.surfaceVariant //TODO "surfaceContainerLowest"
                    ) {
                        Icon(
                            modifier = Modifier
                                .padding(3.dp)
                                .size(20.dp),
                            painter = painterResource(Res.image.ic_like_outlined),
                            contentDescription = "Значок лайка",
                            tint = likeColor
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = status.likes.toString(),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                }
                if (status.trophies != 0) {
                    CircleChip(
                        color = MaterialTheme.colorScheme.surfaceVariant //TODO "surfaceContainerLowest"
                    ) {
                        Icon(
                            modifier = Modifier
                                .padding(3.dp)
                                .size(20.dp),
                            painter = painterResource(Res.image.ic_trophy),
                            contentDescription = "Значок награды",
                            tint = trophyColor
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = status.trophies.toString(),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortraitContent(component: FanficPageInfoComponent) {
    val state by component.state.subscribeAsState()
    val fanfic = remember(state) { state.fanfic }
    val isLoading = remember(state) { state.isLoading }

    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded
    )
    val snackbarHostState = remember { SnackbarHostState() }
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if(fanfic != null && !isLoading) {
            BottomSheetScaffold(
                modifier = Modifier,
                scaffoldState = bottomSheetScaffoldState,
                sheetPeekHeight = 110.dp,
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
                    AnimatedVisibility(
                        visible = bottomSheetState.currentValue == SheetValue.PartiallyExpanded,
                        enter = fadeIn(animationSpec = tween(100)) +
                                expandVertically(animationSpec = tween(100)),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        BottomSheetContentClosed(
                            fanficPage = fanfic,
                            onReadClicked = {
                                component.onOutput(
                                    FanficPageInfoComponent.Output.OpenLastOrFirstChapter(fanfic.chapters)
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    BottomSheetContentOpened(
                        fanficPage = fanfic,
                        onChapterClicked = { index ->
                            component.onOutput(
                                FanficPageInfoComponent.Output.OpenChapter(
                                    index = index,
                                    chapters = fanfic.chapters
                                )
                            )
                        },
                        onCommentClicked = { href ->
                            component.onOutput(
                                FanficPageInfoComponent.Output.OpenComments(href = href)
                            )
                        }
                    )
                },
                topBar = {
                    CollapsingsToolbar(
                        scrollBehavior = scrollBehavior,
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    component.onOutput(
                                        FanficPageInfoComponent.Output.ClosePage
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowBack,
                                    contentDescription = "Стрелка назад"
                                )
                            }
                        },
                        collapsingTitle = CollapsingTitle.small(fanfic.name),
                        modifier = Modifier.background(Color.Transparent)
                    )
                }
            ) { padding ->
                FanficDescription(
                    component = component,
                    fanfic = fanfic,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.35F)
            ) {
                BottomSheetContentClosed(fanfic) {
                    component.onOutput(
                        FanficPageInfoComponent.Output.OpenLastOrFirstChapter(fanfic.chapters)
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                BottomSheetContentOpened(
                    modifier = Modifier.padding(end = 4.dp),
                    fanficPage = fanfic,
                    onChapterClicked = { index ->
                        component.onOutput(
                            FanficPageInfoComponent.Output.OpenChapter(
                                index = index,
                                chapters = fanfic.chapters
                            )
                        )
                    },
                    onCommentClicked = { href ->
                        component.onOutput(
                            FanficPageInfoComponent.Output.OpenComments(href = href)
                        )
                    }
                )
            }
            Scaffold(
                topBar = {
                    CollapsingsToolbar(
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    component.onOutput(
                                        FanficPageInfoComponent.Output.ClosePage
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowBack,
                                    contentDescription = "Стрелка назад"
                                )
                            }
                        },
                        collapsingTitle = CollapsingTitle.small(fanfic.name)
                    )
                }
            ) { padding ->
                FanficDescription(
                    component = component,
                    fanfic = fanfic,
                    modifier = Modifier.padding(
                        top = padding.calculateTopPadding()
                    )
                )
            }
        }
    }
}

@Composable
private fun FanficDescription(
    component: FanficPageInfoComponent,
    fanfic: FanficPageModelStable,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .then(modifier)
                .align(Alignment.TopStart)
                .padding(DefaultPadding.CardDefaultPadding)
                .padding(end = 4.dp)
                .fillMaxSize(),
            state = lazyListState
        ) {
            item {
                FanficHeader(fanficPage = fanfic)
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
                )
            }
            item {
                Pairings(
                    pairings = fanfic.pairings,
                    onPairingClick = { pairing ->
                        //TODO realize onPairingClick
                        println("Clicked pairing: ${pairing.character}")
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
                    onClick = { url ->
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
    tags: List<FanficTagStable>
) {
    FlowRow(
        modifier = modifier
    ) {
        tags.forEach { tag ->
            FanficTagChip(tag = tag)
        }
    }
}

@Composable
private fun BottomSheetContentClosed(
    fanficPage: FanficPageModelStable,
    onReadClicked: () -> Unit
) {
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

        Button(
            onClick = onReadClicked

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
    }
}

@Composable
private
fun BottomSheetContentOpened(
    modifier: Modifier = Modifier,
    fanficPage: FanficPageModelStable,
    onChapterClicked: (index: Int) -> Unit,
    onCommentClicked: (href: String) -> Unit
) {
    val lazyListState = rememberLazyListState()
    Box {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth(),
            state = lazyListState
        ) {
            itemsIndexed(fanficPage.chapters) { index, item ->
                if(item is FanficChapterStable.SeparateChapterModel) {
                    ChapterItem(
                        index = index+1,
                        chapter = item,
                        isReaded = item.readed,
                        onCommentsClicked = {
                            onCommentClicked(item.commentsHref)
                        },
                        onClick = {
                            onChapterClicked(index)
                        }
                    )
                }
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
    chapter: FanficChapterStable.SeparateChapterModel,
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