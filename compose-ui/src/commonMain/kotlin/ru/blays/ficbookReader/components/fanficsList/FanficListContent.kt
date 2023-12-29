package ru.blays.ficbookReader.components.fanficsList

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pullrefresh.PullRefreshIndicator
import androidx.compose.material3.pullrefresh.pullRefresh
import androidx.compose.material3.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.example.myapplication.compose.Res
import com.moriatsushi.insetsx.systemBarsPadding
import ru.blays.ficbookReader.shared.data.dto.SectionWithQuery
import ru.blays.ficbookReader.shared.ui.fanficListComponents.FanficsListComponent
import ru.blays.ficbookReader.ui_components.FanficComponents.FanficCard
import ru.blays.ficbookReader.ui_components.Scrollbar.VerticalScrollbar
import ru.blays.ficbookReader.values.DefaultPadding
import ru.blays.ficbookReader.values.defaultScrollbarPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingsToolbar
import ru.hh.toolbar.custom_toolbar.rememberToolbarScrollBehavior

@Composable
fun FanficsListContent(
    component: FanficsListComponent,
    modifier: Modifier = Modifier
) {
    val state by component.state.subscribeAsState()
    val list = state.list
    val isLoading = state.isLoading

    val lazyListState = rememberLazyListState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = {
            println("onRefresh")
            component.sendIntent(FanficsListComponent.Intent.Refresh)
        }
    )

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
        modifier = Modifier
    ) {
        LazyColumn(
            modifier = Modifier
                .pullRefresh(pullRefreshState)
                .then(modifier)
                .fillMaxSize()
                .padding(DefaultPadding.CardDefaultPadding)
                .padding(end = defaultScrollbarPadding),
            state = lazyListState,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = list,
                key = { it.href }
            ) { fanfic ->
                FanficCard(
                    fanfic = fanfic,
                    onCardClick = {
                        component.onOutput(
                            FanficsListComponent.Output.OpenFanfic(fanfic.href)
                        )
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
        PullRefreshIndicator(
            refreshing = isLoading,
            state = pullRefreshState,
            contentColor = MaterialTheme.colorScheme.primary,
            scale = true,
            modifier = Modifier.align(Alignment.TopCenter)
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
    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            CollapsingsToolbar(
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
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        FanficsListContent(
            component = component,
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        )
    }
}