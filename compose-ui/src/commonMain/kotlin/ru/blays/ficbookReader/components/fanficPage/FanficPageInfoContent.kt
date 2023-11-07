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
import androidx.compose.material3.*
import androidx.compose.material3.pullrefresh.PullRefreshIndicator
import androidx.compose.material3.pullrefresh.pullRefresh
import androidx.compose.material3.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.example.myapplication.compose.Res
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch
import ru.blays.ficbookReader.platformUtils.BackHandler
import ru.blays.ficbookReader.platformUtils.WindowSize
import ru.blays.ficbookReader.shared.data.dto.*
import ru.blays.ficbookReader.shared.ui.fanficPageComponents.declaration.FanficPageInfoComponent
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
    FlowRow {
        fanficPage.authors.forEach { userModel ->
            AuthorItem(
                userModel = userModel,
                avatarSize = 40.dp,
                onAuthorClick = { user ->
                    // TODO
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
    Spacer(modifier = Modifier.height(8.dp))

    if(fanficPage.coverUrl.isNotEmpty()) {
        val painter = asyncPainterResource(data = fanficPage.coverUrl)
        var isCoverExpanded by remember {
            mutableStateOf(false)
        }
        val animatedCoverWidth by animateFloatAsState(
            targetValue = if (isCoverExpanded) 1F else 0.4F,
            animationSpec = spring()
        )
        val contrast = 0.65F
        val colorMatrix = floatArrayOf(
            contrast, 0f, 0f, 0f, 0f,
            0f, contrast, 0f, 0f, 0f,
            0f, 0f, contrast, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )

        SubcomposeLayout { constraints ->
            val fullWidth = constraints.maxWidth
            val coverWidth = (fullWidth * animatedCoverWidth).toInt()
            val coverHeight = (coverWidth * 1.5F).toInt()
            val backgroundHeight = coverHeight + 16
            val coverPlaceX = ((fullWidth - coverWidth) / 2F).toInt()

            val backgroundImage = subcompose("background") {
                KamelImage(
                    resource = painter,
                    contentDescription = "Размытый фон обложки",
                    contentScale = ContentScale.FillWidth,
                    colorFilter = ColorFilter.colorMatrix(ColorMatrix(colorMatrix)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .blur(
                            radius = 16.dp,
                            edgeTreatment = BlurredEdgeTreatment.Unbounded
                        )
                )
            }
            val cover = subcompose("cover") {
                KamelImage(
                    modifier = Modifier
                        .clip(CardShape.CardStandalone)
                        .clickable {
                            isCoverExpanded = !isCoverExpanded
                        },
                    resource = painter,
                    contentDescription = "Обложка фанфика",
                    contentScale = ContentScale.Crop
                )
            }

            layout(
                width = constraints.maxWidth,
                height = backgroundHeight
            ) {
                backgroundImage.map {
                    it.measure(
                        Constraints(
                            minWidth = fullWidth,
                            maxWidth = fullWidth,
                            minHeight = backgroundHeight,
                            maxHeight = backgroundHeight
                        )
                    )
                }
                .first()
                .place(0, 0)
                cover.map {
                    it.measure(
                        Constraints(
                            minWidth = coverWidth,
                            maxWidth = coverWidth,
                            minHeight = coverHeight,
                            maxHeight = coverHeight
                        )
                    )
                }
                .first()
                .place(
                    x = coverPlaceX,
                    y = 8
                )
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
                                    painter = painterResource(Res.image.ic_arrow_back),
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
                                    painter = painterResource(Res.image.ic_arrow_back),
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

@Composable
private fun AuthorItem(
    userModel: UserModelStable,
    avatarSize: Dp,
    onAuthorClick: (author: UserModelStable) -> Unit
) {
    val painter = asyncPainterResource(userModel.avatarUrl)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(CircleShape)
            .clickable {
                onAuthorClick(userModel)
            }
    ) {
        KamelImage(
            resource = painter,
            contentDescription = "Аватар автора",
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = userModel.name,
            style = MaterialTheme.typography.labelLarge
        )
    }
}