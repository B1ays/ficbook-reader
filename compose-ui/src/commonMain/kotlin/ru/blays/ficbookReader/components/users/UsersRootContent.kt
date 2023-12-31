package ru.blays.ficbookReader.components.users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.arkivanov.decompose.router.pages.ChildPages
import com.example.myapplication.compose.Res
import com.moriatsushi.insetsx.systemBarsPadding
import io.github.skeptick.libres.compose.painterResource
import ru.blays.ficbookReader.shared.ui.usersComponent.declaration.UsersRootComponent
import ru.blays.ficbookReader.ui_components.decomposePager.Pages
import ru.blays.ficbookReader.values.Zero
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingsToolbar

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun UsersRootContent(component: UsersRootComponent) {
    val pagesState = component.tabs.subscribeAsState()
    val onPageSelected = { index: Int ->
        component.sendIntent(
            UsersRootComponent.Intent.SelectTab(
                index = index
            )
        )
    }
    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            CollapsingsToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.onOutput(
                                UsersRootComponent.Output.NavigateBack
                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.image.ic_arrow_back),
                            contentDescription = "Стрелка назад"
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.large("Авторы")
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier.padding(top = padding.calculateTopPadding()),
        ) {
            if(maxWidth > 600.dp) {
                LandscapeContent(pagesState, onPageSelected)
            } else {
                PortraitContent(pagesState, onPageSelected)
            }
        }
    }
}

@OptIn(ExperimentalDecomposeApi::class)
@Composable
private fun PortraitContent(
    pagerState: State<ChildPages<UsersRootComponent.TabConfig, UsersRootComponent.Tabs>>,
    onPageSelected: (index: Int) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(6.dp)
            .fillMaxSize(),
    ) {
        ChipTabs(
            pagerState = pagerState,
            onPageSelected = onPageSelected
        )
        Spacer(modifier = Modifier.requiredHeight(8.dp))
        UsersPager(pagerState, onPageSelected)
    }
}

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun LandscapeContent(
    pagerState: State<ChildPages<UsersRootComponent.TabConfig, UsersRootComponent.Tabs>>,
    onPageSelected: (index: Int) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(6.dp)
            .fillMaxSize(),
    ) {
        RailTabs(
            pagerState = pagerState,
            onPageSelected = onPageSelected
        )
        Spacer(modifier = Modifier.requiredWidth(8.dp))
        UsersPager(pagerState, onPageSelected)
    }
}

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun UsersPager(
    state: State<ChildPages<UsersRootComponent.TabConfig, UsersRootComponent.Tabs>>,
    onPageSelected: (index: Int) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
    ) {
        val widthFill = when(maxWidth) {
            in 1500.dp..Dp.Infinity -> 0.5F
            in 1200.dp..1500.dp -> 0.6F
            in 1000.dp..1200.dp -> 0.7F
            in 800.dp..1000.dp -> 0.8F
            in 500.dp..800.dp -> 0.9F
            else -> 1F
        }

        Pages(
            pages = state,
            onPageSelected = onPageSelected,
            userScrollEnabled = false,
            modifier = Modifier.fillMaxWidth(widthFill).align(Alignment.Center)
        ) { _, page ->
            when (page) {
                is UsersRootComponent.Tabs.FavouriteAuthors -> FavouriteAuthorsContent(page.component)
                is UsersRootComponent.Tabs.PopularAuthors -> PopularAuthorsContent(page.component)
                is UsersRootComponent.Tabs.SearchAuthors -> SearchUsersContent(page.component)
            }
        }
    }
}

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun ChipTabs(
    modifier: Modifier = Modifier,
    pagerState: State<ChildPages<UsersRootComponent.TabConfig, UsersRootComponent.Tabs>>,
    onPageSelected: (index: Int) -> Unit
) {
    val state by pagerState
    LazyRow(
        modifier = modifier
    ) {
        itemsIndexed(state.items) { index, tab ->
            InputChip(
                selected = state.selectedIndex == index,
                onClick = {
                    onPageSelected(index)
                },
                label = {
                    Text(
                        text = remember(tab) { getTitleForTab(tab.configuration) }
                    )
                },
                modifier = Modifier.padding(3.dp)
            )
        }
    }
}

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun RailTabs(
    modifier: Modifier = Modifier,
    pagerState: State<ChildPages<UsersRootComponent.TabConfig, UsersRootComponent.Tabs>>,
    onPageSelected: (index: Int) -> Unit
) {
    val state by pagerState
    val scrollState = rememberScrollState()
    NavigationRail(
        header = null,
        windowInsets = WindowInsets.Zero,
        modifier = modifier
            .padding(vertical = 3.dp)
            .verticalScroll(scrollState)
    ) {
        state.items.forEachIndexed { index, tab ->
            NavigationRailItem(
                icon = {
                    val icon = getIconForTab(tab.configuration)
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    val title = remember(tab) { getTitleForTab(tab.configuration) }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                selected = state.selectedIndex == index,
                onClick = {
                    onPageSelected(index)
                }
            )
        }
    }
}

@Composable
private fun getIconForTab(tabs: UsersRootComponent.TabConfig): Painter {
    return when(tabs) {
        UsersRootComponent.TabConfig.FavouriteAuthors -> painterResource(Res.image.ic_star_outlined)
        UsersRootComponent.TabConfig.PopularAuthors -> painterResource(Res.image.ic_flame)
        UsersRootComponent.TabConfig.SearchAuthors -> painterResource(Res.image.ic_search)
    }
}

private fun getTitleForTab(tab: UsersRootComponent.TabConfig): String {
    return when(tab) {
        UsersRootComponent.TabConfig.FavouriteAuthors -> "Избранные авторы"
        UsersRootComponent.TabConfig.PopularAuthors -> "Популярные авторы"
        UsersRootComponent.TabConfig.SearchAuthors -> "Поиск авторов"
    }
}