package ru.blays.ficbook.components.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ficbook_reader.`compose-ui`.generated.resources.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.reader.shared.data.dto.CollectionModelStable
import ru.blays.ficbook.reader.shared.ui.mainScreenComponents.declaration.CollectionsComponent
import ru.blays.ficbook.theme.lockColor
import ru.blays.ficbook.theme.unlockColor
import ru.blays.ficbook.ui_components.PullToRefresh.PullToRefreshContainer
import ru.blays.ficbook.values.CardShape
import ru.blays.ficbook.values.DefaultPadding

@Composable
fun CollectionsComponent(
    component: CollectionsComponent,
    contentPadding: PaddingValues?
) {
    val state by component.state.subscribeAsState()
    val list = remember(state) { state.list }
    val isLoading = remember(state) { state.isLoading }

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
                CollectionsComponent.Intent.Refresh
            )
        }
    }

    BoxWithConstraints {
        LazyVerticalGrid(
            columns = GridCells.Fixed(
                when(maxWidth) {
                    in 800.dp..Dp.Infinity -> 3
                    in 500.dp..800.dp -> 2
                    else -> 1
                }
            ),
            contentPadding = contentPadding ?: PaddingValues(0.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .padding(DefaultPadding.CardDefaultPadding)
                .fillMaxSize()
                .nestedScroll(pullRefreshState.nestedScrollConnection),
        ) {
            items(list) { collection ->
                CollectionItem(collection) {
                    component.onOutput(
                        CollectionsComponent.Output.OpenCollection(
                            name = collection.name,
                            href = collection.href,
                        )
                    )
                }
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
fun CollectionItem(
    collectionModel: CollectionModelStable,
    onClick: () -> Unit
) {
    val indicatorIcon = if(collectionModel.private) {
        painterResource(Res.drawable.ic_lock)
    } else {
        painterResource(Res.drawable.ic_unlock)
    }
    val indicatorIconColor = if(collectionModel.private) lockColor else unlockColor

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(0.7F, false)
            ) {
                Icon(
                    painter = indicatorIcon,
                    contentDescription = "Иконка замок",
                    tint = indicatorIconColor,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = collectionModel.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_user),
                            contentDescription = stringResource(Res.string.content_description_icon_user),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = collectionModel.owner.name,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        MaterialTheme.colorScheme.background,
                        CardShape.CardStandalone,
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = collectionModel.size.toString(),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}