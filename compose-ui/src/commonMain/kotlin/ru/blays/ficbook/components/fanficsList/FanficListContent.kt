package ru.blays.ficbook.components.fanficsList

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import ficbook_reader.compose_ui.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.reader.shared.components.fanficListComponents.declaration.FanficQuickActionsComponent
import ru.blays.ficbook.reader.shared.components.fanficListComponents.declaration.FanficsListComponent
import ru.blays.ficbook.reader.shared.data.dto.SectionWithQuery
import ru.blays.ficbook.ui_components.ContextMenu.ContextMenu
import ru.blays.ficbook.ui_components.ContextMenu.ContextMenuState
import ru.blays.ficbook.ui_components.ContextMenu.contextMenuAnchor
import ru.blays.ficbook.ui_components.ContextMenu.rememberContextMenuState
import ru.blays.ficbook.ui_components.FAB.ScrollToStartFAB
import ru.blays.ficbook.ui_components.FanficComponents.FanficCard2
import ru.blays.ficbook.ui_components.Scrollbar.VerticalScrollbar
import ru.blays.ficbook.utils.LocalGlassEffectConfig
import ru.blays.ficbook.utils.thenIf
import ru.blays.ficbook.values.DefaultPadding
import ru.blays.ficbook.values.defaultScrollbarPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingToolbar
import ru.hh.toolbar.custom_toolbar.rememberToolbarScrollBehavior

@Composable
fun FanficsListContent(
    component: FanficsListComponent,
    lazyListState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues? = null,
    modifier: Modifier = Modifier
) {
    val state by component.state.subscribeAsState()
    val list = state.list
    val isLoading = state.isLoading

    val pullRefreshState = rememberPullToRefreshState()

    val canScrollForward = lazyListState.canScrollForward
    val canScrollBackward = lazyListState.canScrollBackward

    val padding = contentPadding ?: DefaultPadding.Zero

    LaunchedEffect(canScrollForward) {
        if(!canScrollForward && canScrollBackward) {
            component.sendIntent(
                FanficsListComponent.Intent.LoadNextPage
            )
        }
    }

    PullToRefreshBox(
        state = pullRefreshState,
        isRefreshing = isLoading,
        onRefresh = {
            component.sendIntent(
                FanficsListComponent.Intent.Refresh
            )
        },
        indicator = {
            PullToRefreshDefaults.Indicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(padding),
                state = pullRefreshState,
                isRefreshing = isLoading,
                color = MaterialTheme.colorScheme.primary
            )
        }
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(DefaultPadding.CardDefaultPadding)
                .padding(end = defaultScrollbarPadding),
            state = lazyListState,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = padding
        ) {
            items(items = list) { fanfic ->
                val contextMenuState = rememberContextMenuState()

                FanficQuickActions(
                    contextMenuState = contextMenuState,
                    componentFactory = {
                        component.getQuickActionsComponent(fanficID = fanfic.id)
                    }
                )

                FanficCard2(
                    modifier = Modifier.contextMenuAnchor(contextMenuState),
                    fanfic = fanfic,
                    onClick = {
                        component.onOutput(
                            FanficsListComponent.Output.OpenFanfic(fanfic.href)
                        )
                    },
                    onLongClick = {
                        contextMenuState.show()
                    },
                    onPairingClick = { pairing ->
                        component.onOutput(
                            FanficsListComponent.Output.OpenAnotherSection(
                                section = SectionWithQuery(
                                    name = pairing.character,
                                    href = pairing.href
                                )
                            )
                        )
                    },
                    onFandomClick = { fandom ->
                        component.onOutput(
                            FanficsListComponent.Output.OpenAnotherSection(
                                section = SectionWithQuery(
                                    name = fandom.name,
                                    href = fandom.href
                                )
                            )
                        )
                    },
                    onAuthorClick = { author ->
                        component.onOutput(
                            FanficsListComponent.Output.OpenAuthor(
                                href = author.href
                            )
                        )
                    },
                    onUrlClicked = { url ->
                        component.onOutput(
                            FanficsListComponent.Output.OpenUrl(url)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(7.dp))
            }
        }
        VerticalScrollbar(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp)
                .fillMaxHeight(),
            lazyListState = lazyListState
        )
    }
}

@Composable
fun FanficsListScreenContent(
    component: FanficsListComponent
) {
    val state by component.state.subscribeAsState()
    val lazyListState = rememberLazyListState()
    val scrollBehavior = rememberToolbarScrollBehavior()
    val hazeState = remember { HazeState() }
    val blurConfig = LocalGlassEffectConfig.current
    Scaffold(
        topBar = {
            CollapsingToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.onOutput(
                                FanficsListComponent.Output.NavigateBack
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
                    var clicked by remember { mutableStateOf(false) }
                    val transition = updateTransition(
                        targetState = clicked
                    )
                    val angle by transition.animateFloat(
                        transitionSpec = {
                            tween(1000)
                        }
                    ) {
                        if(it) 180f else 0f
                    }
                    val scale by animateFloatAsState(
                        targetValue = if (clicked) 1.2f else 1f,
                        animationSpec = tween(1000),
                        finishedListener = {
                            clicked = false
                        }
                    )
                    IconButton(
                        onClick = {
                            clicked = true
                            component.sendIntent(
                                FanficsListComponent.Intent.SetAsFeed(state.section)
                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_star_filled),
                            contentDescription = stringResource(Res.string.content_description_icon_star),
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(angle)
                                .scale(scale)
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.small(state.section.name),
                scrollBehavior = scrollBehavior,
                insets = WindowInsets.statusBars,
                containerColor = if(blurConfig.blurEnabled) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.surface
                },
                collapsedElevation = if(blurConfig.blurEnabled) 0.dp else 4.dp,
                modifier = Modifier.thenIf(blurConfig.blurEnabled) {
                    hazeChild(
                        state = hazeState,
                        style = blurConfig.style
                    )
                }
            )
        },
        floatingActionButton = { ScrollToStartFAB(lazyListState) },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        FanficsListContent(
            component = component,
            lazyListState = lazyListState,
            contentPadding = padding,
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .thenIf(blurConfig.blurEnabled) {
                    haze(
                        state = hazeState,
                        style = blurConfig.style
                    )
                }
        )
    }
}

@Composable
fun FanficQuickActions(
    contextMenuState: ContextMenuState,
    componentFactory: () -> FanficQuickActionsComponent
) {
    ContextMenu(
        state = contextMenuState
    ) {
        val component = componentFactory()
        val state by component.state.subscribeAsState()

        LaunchedEffect(Unit) {
            component.sendIntent(
                FanficQuickActionsComponent.Intent.Initialize
            )
        }

        if(!state.loading && !state.error) {
            DropdownMenuItem(
                text = {
                    Text(text = stringResource(Res.string.like))
                },
                leadingIcon = {
                    val icon = if(state.liked) {
                        painterResource(Res.drawable.ic_like_filled)
                    } else {
                        painterResource(Res.drawable.ic_like_outlined)
                    }
                    Icon(
                        painter = icon,
                        contentDescription = stringResource(Res.string.content_description_icon_like),
                        modifier = Modifier.size(20.dp),
                    )
                },
                onClick = {
                    component.sendIntent(
                        FanficQuickActionsComponent.Intent.Like
                    )
                },
            )
            DropdownMenuItem(
                text = {
                    Text(text = stringResource(Res.string.subscription))
                },
                leadingIcon = {
                    val icon = if(state.subscribed) {
                        painterResource(Res.drawable.ic_star_filled)
                    } else {
                        painterResource(Res.drawable.ic_star_outlined)
                    }
                    Icon(
                        painter = icon,
                        contentDescription = stringResource(Res.string.content_description_icon_star),
                        modifier = Modifier.size(20.dp),
                    )
                },
                onClick = {
                    component.sendIntent(
                        FanficQuickActionsComponent.Intent.Subscribe
                    )
                },
            )
            DropdownMenuItem(
                text = {
                    Text(text = stringResource(Res.string.readed))
                },
                leadingIcon = {
                    val icon = if(state.readed) {
                        painterResource(Res.drawable.ic_book_filled)
                    } else {
                        painterResource(Res.drawable.ic_book_outlined)
                    }
                    Icon(
                        painter = icon,
                        contentDescription = stringResource(Res.string.content_description_icon_book),
                        modifier = Modifier.size(20.dp),
                    )
                },
                onClick = {
                    component.sendIntent(
                        FanficQuickActionsComponent.Intent.Read
                    )
                },
            )
        } else if (state.error) {
            DropdownMenuItem(
                text = {
                    Text(text = stringResource(Res.string.error))
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_error),
                        contentDescription = stringResource(Res.string.content_description_icon_error),
                        modifier = Modifier.size(20.dp),
                    )
                },
                onClick = {}
            )
        } else {
            DropdownMenuItem(
                text = {
                    Text(text = stringResource(Res.string.loading_in_progress))
                },
                leadingIcon = {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                    )
                },
                onClick = {}
            )
        }
    }
}