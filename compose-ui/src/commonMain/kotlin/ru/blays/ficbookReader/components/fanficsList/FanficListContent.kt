package ru.blays.ficbookReader.components.fanficsList

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pullrefresh.PullRefreshIndicator
import androidx.compose.material3.pullrefresh.pullRefresh
import androidx.compose.material3.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import ru.blays.ficbookReader.shared.ui.fanficListComponents.FanficsListComponent
import ru.blays.ficbookReader.ui_components.FanficComponents.FanficCard
import ru.blays.ficbookReader.ui_components.Scrollbar.VerticalScrollbar
import ru.blays.ficbookReader.values.DefaultPadding
import ru.hh.toolbar.custom_toolbar.CollapsedToolbar
import ru.hh.toolbar.custom_toolbar.CollapsingTitle

@Composable
fun FanficsListContent(
    component: FanficsListComponent,
    modifier: Modifier = Modifier
) {
    val state by component.state.subscribeAsState()
    val list = remember(state) { state.list }
    val isLoading = remember(state) { state.isLoading }

    val lazyListState = rememberLazyListState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = {
            component.sendIntent(FanficsListComponent.Intent.Refresh)
        }
    )

    val canScrollForward = lazyListState.canScrollForward

    LaunchedEffect(canScrollForward) {
        if(!canScrollForward) {
            component.sendIntent(
                FanficsListComponent.Intent.LoadNextPage
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(DefaultPadding.CardDefaultPadding)
                .padding(end = 4.dp)
                .pullRefresh(
                    state = pullRefreshState
                ),
            state = lazyListState
        ) {
            items(list) { fanfic ->
                FanficCard(fanfic = fanfic) {
                    component.onOutput(
                        FanficsListComponent.Output.OpenFanfic(fanfic.href)
                    )
                }
                Spacer(modifier = Modifier.height(7.dp))
            }
        }
        PullRefreshIndicator(
            refreshing = isLoading,
            state = pullRefreshState,
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
    Scaffold(
        topBar = {
            CollapsedToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.onOutput(
                                FanficsListComponent.Output.NavigateBack
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Стрелка назад"
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.small(state.section.name)
            )
        }
    ) { padding ->
        FanficsListContent(
            component = component,
            modifier = Modifier.padding(top = padding.calculateTopPadding())
        )
    }
}