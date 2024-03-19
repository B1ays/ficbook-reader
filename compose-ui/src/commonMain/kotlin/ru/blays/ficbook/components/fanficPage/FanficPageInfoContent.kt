package ru.blays.ficbook.components.fanficPage

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import ficbook_reader.compose_ui.generated.resources.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.platformUtils.BackHandler
import ru.blays.ficbook.platformUtils.WindowSize
import ru.blays.ficbook.reader.feature.copyImageFeature.copyImageToClipboard
import ru.blays.ficbook.reader.shared.components.fanficPageComponents.declaration.FanficPageInfoComponent
import ru.blays.ficbook.reader.shared.components.snackbarStateHost.SnackbarHost
import ru.blays.ficbook.reader.shared.data.dto.*
import ru.blays.ficbook.reader.shared.platformUtils.shareSupported
import ru.blays.ficbook.theme.color
import ru.blays.ficbook.theme.flameGradient
import ru.blays.ficbook.theme.likeColor
import ru.blays.ficbook.theme.trophyColor
import ru.blays.ficbook.ui_components.CustomBottomSheetScaffold.BottomSheetScaffold
import ru.blays.ficbook.ui_components.CustomBottomSheetScaffold.SheetValue
import ru.blays.ficbook.ui_components.CustomBottomSheetScaffold.rememberBottomSheetScaffoldState
import ru.blays.ficbook.ui_components.CustomBottomSheetScaffold.rememberStandardBottomSheetState
import ru.blays.ficbook.ui_components.FanficComponents.CircleChip
import ru.blays.ficbook.ui_components.FanficComponents.FanficTagChip
import ru.blays.ficbook.ui_components.GradientIcon.GradientIcon
import ru.blays.ficbook.ui_components.HyperlinkText.HyperlinkText
import ru.blays.ficbook.ui_components.PullToRefresh.PullToRefreshContainer
import ru.blays.ficbook.ui_components.Scrollbar.VerticalScrollbar
import ru.blays.ficbook.ui_components.spacers.HorizontalSpacer
import ru.blays.ficbook.ui_components.spacers.VerticalSpacer
import ru.blays.ficbook.utils.LocalGlassEffectConfig
import ru.blays.ficbook.utils.primaryColorAtAlpha
import ru.blays.ficbook.utils.thenIf
import ru.blays.ficbook.values.CardShape
import ru.blays.ficbook.values.DefaultPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingToolbar
import ru.hh.toolbar.custom_toolbar.rememberToolbarScrollBehavior

@Composable
fun FanficPageInfoContent(component: FanficPageInfoComponent) {
    val windowSize = WindowSize()

    if (windowSize.width > 600) {
        LandscapeContent(component)
    } else {
        PortraitContent(component)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
private fun PortraitContent(component: FanficPageInfoComponent) {
    val state by component.state.subscribeAsState()
    val fanfic = state.fanfic
    val isLoading = state.isLoading

    val glassEffectConfig = LocalGlassEffectConfig.current

    val scope = rememberCoroutineScope()

    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded
    )
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = bottomSheetState
    )

    val pullRefreshState = rememberPullToRefreshState()
    val scrollBehavior = rememberToolbarScrollBehavior()

    val transition = updateTransition(
        targetState = bottomSheetState.currentValue,
        label = "BottomSheetTransition"
    )

    val sheetCornerRadius by transition.animateDp { value ->
        when(value) {
            SheetValue.Expanded -> 0.dp
            else -> 16.dp
        }
    }

    val sheetShape = RoundedCornerShape(
        topStart = sheetCornerRadius,
        topEnd = sheetCornerRadius,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )

    LaunchedEffect(isLoading) {
        when {
            isLoading && !pullRefreshState.isRefreshing -> {
                pullRefreshState.startRefresh()
            }

            !isLoading && pullRefreshState.isRefreshing -> {
                pullRefreshState.endRefresh()
            }
        }
    }
    LaunchedEffect(pullRefreshState.isRefreshing) {
        if (pullRefreshState.isRefreshing && !isLoading) {
            component.sendIntent(
                FanficPageInfoComponent.Intent.Refresh
            )
        }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        if (fanfic != null && !isLoading) {
            val coverPainter: AsyncImagePainter? = if (fanfic.coverUrl.isNotEmpty()) {
                rememberAsyncImagePainter(fanfic.coverUrl)
            } else {
                null
            }
            var hazeModifier: Modifier = remember { Modifier }

            if (
                coverPainter != null &&
                glassEffectConfig.blurEnabled
            ) {
                val hazeState = remember { HazeState() }

                hazeModifier = Modifier.hazeChild(
                    state = hazeState,
                    style = glassEffectConfig.style
                )

                Image(
                    painter = coverPainter,
                    contentDescription = stringResource(Res.string.content_description_fanfic_cover),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .haze(state = hazeState)
                )
            }
            val sheetProgress by remember {
                derivedStateOf {
                    bottomSheetState.progressBetweenTwoValues(
                        SheetValue.Expanded,
                        SheetValue.PartiallyExpanded
                    )
                }
            }
            val sheetBackground = MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp)
            val sheetBackgroundAtProgress = sheetBackground.copy(
                alpha = sheetProgress
            )

            val navigationBarHeight = WindowInsets.navigationBars
                .asPaddingValues()
                .calculateBottomPadding()

            BottomSheetScaffold(
                scaffoldState = bottomSheetScaffoldState,
                sheetPeekHeight = 110.dp + navigationBarHeight,
                containerColor = Color.Transparent,
                fullscreenSheet = true,
                sheetContainerColor = sheetBackgroundAtProgress,
                sheetShadowElevation = 0.dp,
                sheetShape = sheetShape,
                sheetSwipeEnabled = fanfic.chapters !is FanficChapterStable.SingleChapterModel,
                modifier = hazeModifier,
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
                        transition = transition,
                    ) {
                        component.onOutput(
                            FanficPageInfoComponent.Output.OpenLastOrFirstChapter(
                                fanficID = fanfic.fanficID,
                                chapter = fanfic.chapters
                            )
                        )
                    }

                    VerticalSpacer(navigationBarHeight * (1 - sheetProgress))

                    val chapters = fanfic.chapters
                    if (chapters is FanficChapterStable.SeparateChaptersModel) {
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
                sheetDragHandle = {
                    transition.AnimatedContent(
                        transitionSpec = {
                            slideInVertically { height -> -height } + fadeIn() togetherWith
                                slideOutVertically { height -> -height } + fadeOut()
                        }
                    ) { value ->
                        when (value) {
                            SheetValue.Expanded -> Spacer(modifier = Modifier.statusBarsPadding())
                            else -> BottomSheetDefaults.DragHandle()
                        }
                    }
                },
                topBar = {
                    CollapsingToolbar(
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
                                    painter = painterResource(Res.drawable.ic_arrow_back),
                                    contentDescription = stringResource(Res.string.content_description_icon_back)
                                )
                            }
                        },
                        actions = { TopBarActions(component) },
                        collapsingTitle = CollapsingTitle.medium(fanfic.name),
                        insets = WindowInsets.statusBars
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
                            bottom = padding.calculateBottomPadding()
                        )
                        .nestedScroll(pullRefreshState.nestedScrollConnection)
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                )
            }
        }
        PullToRefreshContainer(
            state = pullRefreshState,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}


@OptIn(ExperimentalResourceApi::class)
@Composable
private fun LandscapeContent(
    component: FanficPageInfoComponent
) {
    val state by component.state.subscribeAsState()
    val fanfic = state.fanfic
    val isLoading = state.isLoading

    val glassEffectConfig = LocalGlassEffectConfig.current

    if (fanfic != null && !isLoading) {
        val coverPainter: AsyncImagePainter? = if (fanfic.coverUrl.isNotEmpty()) {
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
                    fanficPage = fanfic
                ) {
                    component.onOutput(
                        FanficPageInfoComponent.Output.OpenLastOrFirstChapter(
                            fanficID = fanfic.fanficID,
                            chapter = fanfic.chapters
                        )
                    )
                }
                val chapters = fanfic.chapters
                if (chapters is FanficChapterStable.SeparateChaptersModel) {
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
                var scaffoldModifier: Modifier = remember { Modifier }

                if (
                    coverPainter != null &&
                    glassEffectConfig.blurEnabled
                ) {
                    val hazeState = remember { HazeState() }

                    scaffoldModifier = Modifier.hazeChild(
                        state = hazeState,
                        style = glassEffectConfig.style
                    )

                    Image(
                        painter = coverPainter,
                        contentDescription = stringResource(Res.string.content_description_fanfic_cover),
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxSize()
                            .haze(hazeState)
                    )
                }
                Scaffold(
                    modifier = scaffoldModifier.systemBarsPadding(),
                    topBar = {
                        CollapsingToolbar(
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
                                        painter = painterResource(Res.drawable.ic_arrow_back),
                                        contentDescription = stringResource(Res.string.content_description_icon_back)
                                    )
                                }
                            },
                            actions = {
                                TopBarActions(component)
                            },
                            collapsingTitle = CollapsingTitle.small(fanfic.name)
                        )
                    },
                    containerColor = Color.Transparent,
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalResourceApi::class, ExperimentalCoilApi::class)
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

    if (coverPainter != null) {
        var isCoverExpanded by remember {
            mutableStateOf(false)
        }
        val animatedCoverWidth by animateFloatAsState(
            targetValue = if (isCoverExpanded) 1F else 0.4F,
            animationSpec = spring()
        )

        val scope = rememberCoroutineScope { Dispatchers.IO }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Image(
                painter = coverPainter,
                contentDescription = stringResource(Res.string.content_description_fanfic_cover),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth(animatedCoverWidth)
                    .aspectRatio(1F / 1.5F)
                    .clip(CardShape.CardStandalone)
                    .shadow(
                        elevation = 4.dp,
                        shape = CardShape.CardStandalone,
                        clip = true
                    )
                    .combinedClickable(
                        onClick = { isCoverExpanded = !isCoverExpanded },
                        onLongClick = {
                            when (val state = coverPainter.state) {
                                is AsyncImagePainter.State.Success -> {
                                    scope.launch {
                                        val success = copyImageToClipboard(state.result.image)
                                        if (success) {
                                            SnackbarHost.showMessage(
                                                message = getString(Res.string.cover_copied)
                                            )
                                        }
                                    }
                                }
                                else -> {}
                            }
                        }
                    )
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun CollectionsInfo(
    inCollectionsCount: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.primary
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primaryColorAtAlpha(0.2F))
                    .clickable(onClick = onClick)
                    .padding(3.dp),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_stack),
                    contentDescription = stringResource(Res.string.content_description_icon_stack),
                    modifier = Modifier.size(20.dp),
                )
                HorizontalSpacer(4.dp)
                Text(
                    text = stringResource(Res.string.fanficPage_in_collections, inCollectionsCount),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
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
                FanficInfo(fanfic)
                Spacer(modifier = Modifier.height(2.dp))
            }
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
                FanficActionsContent(component = component.actionsComponent)
                VerticalSpacer(10.dp)
            }
            item {
                CollectionsInfo(
                    inCollectionsCount = fanfic.inCollectionsCount,
                    onClick = {
                        component.onOutput(
                            FanficPageInfoComponent.Output.OpenAssociatedCollections(
                                fanficID = fanfic.fanficID
                            )
                        )
                    }
                )
                VerticalSpacer(6.dp)
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
                    text = stringResource(Res.string.fanficPage_description),
                    style = MaterialTheme.typography.titleMedium
                )
                HyperlinkText(
                    fullText = fanfic.description,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    onLinkClick = { url ->
                        component.onOutput(
                            FanficPageInfoComponent.Output.OpenUrl(url)
                        )
                    }
                )
                VerticalSpacer(6.dp)
            }
            fanfic.dedication?.let { dedication ->
                item {
                    Text(
                        text = stringResource(Res.string.fanficPage_dedication),
                        style = MaterialTheme.typography.titleMedium
                    )
                    HyperlinkText(
                        fullText = dedication,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        onLinkClick = { url ->
                            component.onOutput(
                                FanficPageInfoComponent.Output.OpenUrl(url)
                            )
                        }
                    )
                    VerticalSpacer(6.dp)
                }
            }
            fanfic.authorComment?.let { authorComment ->
                item {
                    Text(
                        text = stringResource(Res.string.fanficPage_author_comment),
                        style = MaterialTheme.typography.titleMedium
                    )
                    HyperlinkText(
                        fullText = authorComment,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        onLinkClick = { url ->
                            component.onOutput(
                                FanficPageInfoComponent.Output.OpenUrl(url)
                            )
                        }
                    )
                    VerticalSpacer(6.dp)
                }
            }
            item {
                Text(
                    text = stringResource(Res.string.fanficPage_publication_rules),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = fanfic.publicationRules,
                    style = MaterialTheme.typography.bodyLarge
                )
                VerticalSpacer(6.dp)
            }
            items(fanfic.rewards) { reward ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier
                            .padding(3.dp)
                            .size(18.dp),
                        painter = painterResource(Res.drawable.ic_trophy),
                        contentDescription = stringResource(Res.string.content_description_icon_reward),
                        tint = trophyColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = stringResource(Res.string.fanficPage_reward_from, reward.message, reward.fromUser),
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalResourceApi::class)
@Composable
private fun Fandoms(
    fandoms: List<FandomModelStable>,
    onFandomClick: (fandom: FandomModelStable) -> Unit
) {
    FlowRow {
        Text(
            text = stringResource(Res.string.fanficPage_fandoms),
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalResourceApi::class)
@Composable
private fun Pairings(
    pairings: List<PairingModelStable>,
    onPairingClick: (pairing: PairingModelStable) -> Unit
) {
    if(pairings.isNotEmpty()) {
        FlowRow {
            val shape = remember { RoundedCornerShape(percent = 20) }
            Text(
                text = stringResource(Res.string.fanficPage_pairings_and_characters),
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
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalResourceApi::class)
@Composable
private fun FanficInfo(fanfic: FanficPageModelStable) {
    val status = fanfic.status
    FlowRow(
        verticalArrangement = Arrangement.Center,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (status.direction != FanficDirection.UNKNOWN) {
            CircleChip(
                color = MaterialTheme.colorScheme.surfaceVariant,
                minSize = 27.dp
            ) {
                Icon(
                    modifier = Modifier
                        .padding(3.dp)
                        .size(20.dp)
                        .scale(1.5F),
                    painter = getIconForDirection(status.direction),
                    contentDescription = stringResource(Res.string.content_description_icon_direction),
                    tint = status.direction.color
                )
            }
        }
        if (status.rating != FanficRating.UNKNOWN) {
            CircleChip(
                color = MaterialTheme.colorScheme.surfaceVariant,
                minSize = 27.dp
            ) {
                Text(
                    text = status.rating.rating,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        if (status.status != FanficCompletionStatus.UNKNOWN) {
            CircleChip(
                color = MaterialTheme.colorScheme.surfaceVariant,
                minSize = 27.dp
            ) {
                Icon(
                    modifier = Modifier
                        .padding(3.dp)
                        .size(20.dp),
                    painter = getIconForStatus(status.status),
                    contentDescription = stringResource(Res.string.content_description_icon_status),
                    tint = status.status.color

                )
            }
        }
        if (status.likes != 0) {
            CircleChip(
                color = MaterialTheme.colorScheme.surfaceVariant,
                minSize = 27.dp
            ) {
                Icon(
                    modifier = Modifier
                        .padding(3.dp)
                        .size(20.dp),
                    painter = painterResource(Res.drawable.ic_like_outlined),
                    contentDescription = stringResource(Res.string.content_description_icon_like),
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
        if (status.hot) {
            CircleChip(
                color = MaterialTheme.colorScheme.surfaceVariant,
                minSize = 27.dp
            ) {
                GradientIcon(
                    modifier = Modifier
                        .padding(3.dp)
                        .size(20.dp),
                    painter = painterResource(Res.drawable.ic_flame),
                    contentDescription = stringResource(Res.string.content_description_icon_flame),
                    brush = flameGradient
                )
            }
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

@OptIn(ExperimentalResourceApi::class, ExperimentalAnimationApi::class)
@Composable
private fun BottomSheetContentClosed(
    component: FanficPageInfoComponent,
    fanficPage: FanficPageModelStable,
    transition: Transition<SheetValue>? = null,
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
                text = stringResource(Res.string.fanficPage_chapters_count, fanficPage.chapters.size),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(Res.string.fanficPage_pages_count, fanficPage.pagesCount),
                style = MaterialTheme.typography.titleMedium
            )
        }

        if(transition != null) {
            transition.AnimatedContent(
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> width } + fadeOut()
                },
                contentAlignment = Alignment.Center
            ) { value ->
                when(value) {
                    SheetValue.Hidden, SheetValue.PartiallyExpanded -> {
                        Button(
                            onClick = onReadClicked,
                        ) {
                            Icon(
                                modifier = Modifier.size(14.dp),
                                painter = painterResource(Res.drawable.ic_open_book),
                                contentDescription = stringResource(Res.string.content_description_icon_book)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = stringResource(Res.string.read)
                            )
                        }
                    }
                    SheetValue.Expanded -> {
                        IconButton(
                            onClick = { menuExpanded = !menuExpanded }
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_more_vertical),
                                contentDescription = stringResource(Res.string.content_description_icon_more),
                                modifier = Modifier.size(20.dp)
                            )
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },

                                ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(text = stringResource(Res.string.fanficPage_reverse_chapters_order))
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
            }
        } else {
            Button(
                onClick = onReadClicked,
            ) {
                Icon(
                    modifier = Modifier.size(14.dp),
                    painter = painterResource(Res.drawable.ic_open_book),
                    contentDescription = stringResource(Res.string.content_description_icon_book)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = stringResource(Res.string.read)
                )
            }
        }
    }

    if(transition != null) {
        transition.AnimatedVisibility(
            visible = { it == SheetValue.Expanded },
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            HorizontalDivider(
                modifier = Modifier
                    .padding(vertical = 2.dp),
                color = MaterialTheme.colorScheme.outline,
                thickness = (1.5F).dp
            )
        }
    } else {
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 2.dp),
            color = MaterialTheme.colorScheme.outline,
            thickness = (1.5F).dp
        )
    }

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
        if (reversed) {
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
                    index = if (reversed) reversedList.size - index else index + 1,
                    chapter = item,
                    isReaded = item.readed,
                    onCommentsClicked = {
                        onCommentClicked(item.chapterID)
                    },
                    onClick = {
                        onChapterClicked(
                            if (reversed) reversedList.lastIndex - index else index
                        )
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

@OptIn(ExperimentalResourceApi::class)
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
                        .thenIf(isReaded) {
                            border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = index.toString(),
                        color = if (isReaded) {
                            MaterialTheme.colorScheme.onSurface
                        }
                        else {
                            MaterialTheme.colorScheme.onPrimary
                        }
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
                color = MaterialTheme.colorScheme.primary, //TODO "surfaceContainerLowest"
                modifier = Modifier.clickable(onClick = onCommentsClicked)
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onPrimary
                ) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        painter = painterResource(Res.drawable.ic_comment),
                        contentDescription = stringResource(Res.string.content_description_icon_comment),
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
}

@OptIn(ExperimentalResourceApi::class)
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
            contentDescription = stringResource(Res.string.content_description_icon_author_avatar),
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

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun TopBarActions(component: FanficPageInfoComponent) {
    var dropDownMenuState by remember { mutableStateOf(false) }
    IconButton(
        onClick = {
            dropDownMenuState = !dropDownMenuState
        }
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_more_vertical),
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
        if (shareSupported) {
            DropdownMenuItem(
                text = {
                    Text(text = stringResource(Res.string.action_share))
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_share),
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
                Text(text = stringResource(Res.string.action_copy_link))
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_link),
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
                Text(text = stringResource(Res.string.action_open_in_browser))
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_globe),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            onClick = {
                dropDownMenuState = false
                component.sendIntent(FanficPageInfoComponent.Intent.OpenInBrowser)
            }
        )
        component.state.value.fanfic?.let {
            DropdownMenuItem(
                text = {
                    Text(text = stringResource(Res.string.download))
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_download),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                onClick = {
                    dropDownMenuState = false
                    component.onOutput(
                        FanficPageInfoComponent.Output.DownloadFanfic(
                            fanficID = component.fanficHref.substringAfterLast('/'),
                            fanficName = it.name
                        )
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun getIconForDirection(direction: FanficDirection): Painter {
    return when (direction) {
        FanficDirection.GEN -> painterResource(Res.drawable.ic_direction_gen)
        FanficDirection.HET -> painterResource(Res.drawable.ic_direction_het)
        FanficDirection.SLASH -> painterResource(Res.drawable.ic_direction_slash)
        FanficDirection.FEMSLASH -> painterResource(Res.drawable.ic_direction_femslash)
        FanficDirection.ARTICLE -> painterResource(Res.drawable.ic_direction_article)
        FanficDirection.MIXED -> painterResource(Res.drawable.ic_direction_mixed)
        FanficDirection.OTHER -> painterResource(Res.drawable.ic_direction_other)
        FanficDirection.UNKNOWN -> painterResource(Res.drawable.ic_direction_other)
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun getIconForStatus(status: FanficCompletionStatus): Painter {
    return when (status) {
        FanficCompletionStatus.IN_PROGRESS -> painterResource(Res.drawable.ic_clock)
        FanficCompletionStatus.COMPLETE -> painterResource(Res.drawable.ic_check)
        FanficCompletionStatus.FROZEN -> painterResource(Res.drawable.ic_snowflake)
        FanficCompletionStatus.UNKNOWN -> painterResource(Res.drawable.ic_dot)
    }
}