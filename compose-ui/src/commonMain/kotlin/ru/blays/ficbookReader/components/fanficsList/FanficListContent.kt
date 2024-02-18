package ru.blays.ficbookReader.components.fanficsList

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.example.myapplication.compose.Res
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import ru.blays.ficbookReader.shared.data.dto.SectionWithQuery
import ru.blays.ficbookReader.shared.ui.fanficListComponents.declaration.FanficQuickActionsComponent
import ru.blays.ficbookReader.shared.ui.fanficListComponents.declaration.FanficsListComponent
import ru.blays.ficbookReader.ui_components.ContextMenu.ContextMenu
import ru.blays.ficbookReader.ui_components.ContextMenu.ContextMenuState
import ru.blays.ficbookReader.ui_components.ContextMenu.contextMenuAnchor
import ru.blays.ficbookReader.ui_components.ContextMenu.rememberContextMenuState
import ru.blays.ficbookReader.ui_components.FanficComponents.FanficCard
import ru.blays.ficbookReader.ui_components.PullToRefresh.PullToRefreshContainer
import ru.blays.ficbookReader.ui_components.Scrollbar.VerticalScrollbar
import ru.blays.ficbookReader.utils.LocalGlassEffectConfig
import ru.blays.ficbookReader.utils.thenIf
import ru.blays.ficbookReader.values.DefaultPadding
import ru.blays.ficbookReader.values.defaultScrollbarPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingToolbar
import ru.hh.toolbar.custom_toolbar.rememberToolbarScrollBehavior

@Composable
fun FanficsListContent(
    component: FanficsListComponent,
    contentPadding: PaddingValues? = null,
    modifier: Modifier = Modifier
) {
    val state by component.state.subscribeAsState()
    val list = state.list
    val isLoading = state.isLoading

    val lazyListState = rememberLazyListState()
    val pullRefreshState = rememberPullToRefreshState()

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
        if(pullRefreshState.isRefreshing && !isLoading) {
            component.sendIntent(
                FanficsListComponent.Intent.Refresh
            )
        }
    }

    val canScrollForward = lazyListState.canScrollForward
    val canScrollBackward = lazyListState.canScrollBackward

    LaunchedEffect(canScrollForward) {
        if(!canScrollForward && canScrollBackward) {
            component.sendIntent(
                FanficsListComponent.Intent.LoadNextPage
            )
        }
    }

    Box(
        modifier = Modifier.nestedScroll(pullRefreshState.nestedScrollConnection),
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(DefaultPadding.CardDefaultPadding)
                .padding(end = defaultScrollbarPadding),
            state = lazyListState,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = contentPadding ?: PaddingValues(0.dp)
        ) {
            items(
                items = list
            ) { fanfic ->
                val contextMenuState = rememberContextMenuState()

                FanficQuickActions(
                    contextMenuState = contextMenuState,
                    componentFactory = {
                        component.getQuickActionsComponent(fanficID = fanfic.id)
                    }
                )

                FanficCard(
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
        PullToRefreshContainer(
            state = pullRefreshState,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = contentPadding?.calculateTopPadding() ?: 0.dp),
        )
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
                            painter = painterResource(Res.image.ic_arrow_back),
                            contentDescription = "Стрелка назад"
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
                            painter = painterResource(Res.image.ic_star_filled),
                            contentDescription = "Иконка добавить в избранное",
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
        }
    ) { padding ->
        FanficsListContent(
            component = component,
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
                    Text(text = "Лайк")
                },
                leadingIcon = {
                    val icon = if(state.liked) {
                        painterResource(Res.image.ic_like_filled)
                    } else {
                        painterResource(Res.image.ic_like_outlined)
                    }
                    Icon(
                        painter = icon,
                        contentDescription = "Лайк",
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
                    Text(text = "Подписка")
                },
                leadingIcon = {
                    val icon = if(state.subscribed) {
                        painterResource(Res.image.ic_star_filled)
                    } else {
                        painterResource(Res.image.ic_star_outlined)
                    }
                    Icon(
                        painter = icon,
                        contentDescription = "Подписка",
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
                    Text(text = "Прочитано")
                },
                leadingIcon = {
                    val icon = if(state.readed) {
                        painterResource(Res.image.ic_book_filled)
                    } else {
                        painterResource(Res.image.ic_book_outlined)
                    }
                    Icon(
                        painter = icon,
                        contentDescription = "Прочитано",
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
                    Text(text = "Ошибка")
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.image.ic_error),
                        contentDescription = "Ошибка",
                        modifier = Modifier.size(20.dp),
                    )
                },
                onClick = {}
            )
        } else {
            DropdownMenuItem(
                text = {
                    Text(text = "Загрузка...")
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