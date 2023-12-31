@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package ru.blays.ficbookReader.components.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.material3.pullrefresh.PullRefreshIndicator
import androidx.compose.material3.pullrefresh.pullRefresh
import androidx.compose.material3.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.example.myapplication.compose.Res
import ru.blays.ficbookReader.shared.data.dto.CollectionModelStable
import ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration.CollectionsComponent
import ru.blays.ficbookReader.theme.lockColor
import ru.blays.ficbookReader.theme.unlockColor
import ru.blays.ficbookReader.values.CardShape
import ru.blays.ficbookReader.values.DefaultPadding

@Composable
fun CollectionsComponent(component: CollectionsComponent) {
    val state by component.state.subscribeAsState()
    val list = remember(state) { state.list }
    val isLoading = remember(state) { state.isLoading }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = {
            component.sendIntent(
                CollectionsComponent.Intent.Refresh
            )
        }
    )

    BoxWithConstraints {
        LazyVerticalGrid(
            columns = GridCells.Fixed(
                when(maxWidth) {
                    in 800.dp..Dp.Infinity -> 3
                    in 500.dp..800.dp -> 2
                    else -> 1
                }
            ),
            modifier = Modifier
                .padding(DefaultPadding.CardDefaultPadding)
                .fillMaxSize()
                .pullRefresh(state = pullRefreshState),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
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
        PullRefreshIndicator(
            modifier = Modifier.align(Alignment.TopCenter),
            refreshing = isLoading,
            state = pullRefreshState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionItem(
    collectionModel: CollectionModelStable,
    onClick: () -> Unit
) {
    val indicatorIcon = if(collectionModel.private) {
        painterResource(Res.image.ic_lock)
    } else {
        painterResource(Res.image.ic_unlock)
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
                            painter = painterResource(Res.image.ic_user),
                            contentDescription = "иконка пользователь",
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