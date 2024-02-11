package ru.blays.ficbookReader.components.collectionContent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.example.myapplication.compose.Res
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import io.github.skeptick.libres.compose.painterResource
import kotlinx.coroutines.launch
import ru.blays.ficbookReader.components.fanficsList.FanficsListContent
import ru.blays.ficbookReader.shared.ui.collectionSortComponent.CollectionFanficsListComponent
import ru.blays.ficbookReader.shared.ui.fanficListComponents.FanficsListComponent
import ru.blays.ficbookReader.ui_components.CustomBottomSheetScaffold.BottomSheetScaffold
import ru.blays.ficbookReader.ui_components.CustomBottomSheetScaffold.SheetValue.Expanded
import ru.blays.ficbookReader.ui_components.CustomBottomSheetScaffold.SheetValue.PartiallyExpanded
import ru.blays.ficbookReader.ui_components.CustomBottomSheetScaffold.rememberBottomSheetScaffoldState
import ru.blays.ficbookReader.ui_components.CustomBottomSheetScaffold.rememberSheetState
import ru.blays.ficbookReader.utils.LocalGlassEffectConfig
import ru.blays.ficbookReader.utils.thenIf
import ru.blays.ficbookReader.values.DefaultPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingToolbar

@Composable
fun CollectionContent(component: CollectionFanficsListComponent) {
    BoxWithConstraints {
        if(maxWidth > 600.dp) {
            LandscapeContent(component)
        } else {
            PortraitContent(component)
        }
    }
}

@Composable
private fun LandscapeContent(component: CollectionFanficsListComponent) {
    val state by component.state.subscribeAsState()
    var sortParamsOpened by rememberSaveable { mutableStateOf(false) }

    val glassEffectConfig = LocalGlassEffectConfig.current
    val hazeState = remember { HazeState() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CollapsingToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.fanficsListComponent.onOutput(
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
                    IconButton(
                        onClick = {
                            sortParamsOpened = !sortParamsOpened
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.image.ic_sort),
                            contentDescription = "Сортировка",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.small(state.collectionName),
                containerColor = if(glassEffectConfig.blurEnabled) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.surface
                },
                collapsedElevation = if(glassEffectConfig.blurEnabled) 0.dp else 4.dp,
                insets = WindowInsets.statusBars,
                modifier = Modifier.thenIf(glassEffectConfig.blurEnabled) {
                    hazeChild(
                        state = hazeState,
                        style = glassEffectConfig.style
                    )
                },
            )
        }
    ) { padding ->
        Box {
            FanficsListContent(
                component = component.fanficsListComponent,
                contentPadding = padding,
                modifier = Modifier.thenIf(glassEffectConfig.blurEnabled) {
                    haze(state = hazeState)
                }
            )
            AnimatedVisibility(
                visible = sortParamsOpened,
                enter = expandHorizontally(spring()),
                exit = shrinkHorizontally(spring()),
                modifier = Modifier
                    .padding(top = padding.calculateTopPadding())
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = DrawerDefaults.shape
                    ),
            ) {
                SortParamContent(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxHeight()
                        .fillMaxWidth(0.4F),
                    component = component,
                ) {
                    sortParamsOpened = false
                }
            }
        }
    }
}

@Composable
private fun PortraitContent(component: CollectionFanficsListComponent) {
    val state by component.state.subscribeAsState()
    val bottomSheetState = rememberSheetState(
        skipPartiallyExpanded = false,
        initialValue = PartiallyExpanded
    )
    val bottomSheetScaffoldState =
        rememberBottomSheetScaffoldState(bottomSheetState)
    val coroutineScope = rememberCoroutineScope()

    val glassEffectConfig = LocalGlassEffectConfig.current
    val hazeState = remember { HazeState() }

    BottomSheetScaffold(
        modifier = Modifier.fillMaxSize(),
        sheetPeekHeight = 0.dp,
        sheetContent = {
            SortParamContent(component = component) {
                coroutineScope.launch {
                    bottomSheetState.partialExpand()
                }
            }
        },
        topBar = {
            CollapsingToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.fanficsListComponent.onOutput(
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
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                if(bottomSheetState.currentValue == Expanded) {
                                    bottomSheetState.partialExpand()
                                } else {
                                    bottomSheetState.expand()
                                }
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.image.ic_sort),
                            contentDescription = "Сортировка",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.small(state.collectionName),
                containerColor = if(glassEffectConfig.blurEnabled) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.surface
                },
                collapsedElevation = if(glassEffectConfig.blurEnabled) 0.dp else 4.dp,
                insets = WindowInsets.statusBars,
                modifier = Modifier.thenIf(glassEffectConfig.blurEnabled) {
                    hazeChild(
                        state = hazeState,
                        style = glassEffectConfig.style
                    )
                },
            )
        },
        scaffoldState = bottomSheetScaffoldState
    ) { padding ->
        FanficsListContent(
            component = component.fanficsListComponent,
            contentPadding = padding,
            modifier = Modifier.thenIf(glassEffectConfig.blurEnabled) {
                haze(state = hazeState)
            },
        )
    }
}

@Composable
private fun SortParamContent(
    modifier: Modifier = Modifier,
    component: CollectionFanficsListComponent,
    onDismissRequest: () -> Unit = {},
) {
    val state by component.state.subscribeAsState()

    var fandomMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var directionMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var sortMenuExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(DefaultPadding.CardDefaultPadding),
    ) {
        TextField(
            value = state.currentParams.searchText ?: "",
            onValueChange = {
                component.onIntent(
                    CollectionFanficsListComponent.Intent.ChangeSearchText(it)
                )
            },
            label = {
                Text(text = "Поиск по названию")
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        component.onIntent(
                            CollectionFanficsListComponent.Intent.ChangeSearchText("")
                        )
                    }
                ) {
                    Icon(
                        painter = painterResource(Res.image.ic_cancel),
                        contentDescription = "Очистить поиск",
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = CardDefaults.shape,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.requiredHeight(10.dp))
        state.availableParams?.availableFandoms?.let { items ->
            DropDownSelector(
                expanded = fandomMenuExpanded,
                onExpandedChange = {
                    fandomMenuExpanded = it
                },
                selectedItemName = state.currentParams.fandom?.first ?: "любой фэндом",
                items = items,
                onItemClicked = { item ->
                    component.onIntent(
                        CollectionFanficsListComponent.Intent.ChangeFandom(item)
                    )
                }
            )
        }
        Spacer(modifier = Modifier.requiredHeight(10.dp))
        state.availableParams?.availableDirections?.let { items ->
            DropDownSelector(
                expanded = directionMenuExpanded,
                onExpandedChange = {
                    directionMenuExpanded = it
                },
                selectedItemName = state.currentParams.direction?.first ?: "любая направленность",
                items = items,
                onItemClicked = { item ->
                    component.onIntent(
                        CollectionFanficsListComponent.Intent.ChangeDirection(item)
                    )
                }
            )
        }
        Spacer(modifier = Modifier.requiredHeight(10.dp))
        state.availableParams?.availableSortParams?.let { items ->
            DropDownSelector(
                expanded = sortMenuExpanded,
                onExpandedChange = {
                    sortMenuExpanded = it
                },
                selectedItemName = state.currentParams.sort?.first ?: "по умолчанию",
                items = items,
                onItemClicked = { item ->
                    component.onIntent(
                        CollectionFanficsListComponent.Intent.ChangeSortType(item)
                    )
                }
            )
        }
        Spacer(modifier = Modifier.requiredHeight(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = {
                    component.onIntent(
                        CollectionFanficsListComponent.Intent.Clear
                    )
                    onDismissRequest()
                }
            ) {
                Icon(
                    painter = painterResource(Res.image.ic_cross),
                    contentDescription = "Иконка очистки",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.requiredWidth(8.dp))
                Text(
                    text = "Сбросить"
                )
            }
            Spacer(modifier = Modifier.requiredWidth(14.dp))
            Button(
                onClick = {
                    component.onIntent(
                        CollectionFanficsListComponent.Intent.Search
                    )
                    onDismissRequest()
                }
            ) {
                Icon(
                    painter = painterResource(Res.image.ic_search),
                    contentDescription = "Иконка поиска",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.requiredWidth(8.dp),)
                Text(text = "Найти")
            }
        }
    }
    Spacer(modifier = Modifier.requiredHeight(20.dp))
}

@Composable
private fun DropDownSelector(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    selectedItemName: String,
    items: List<Pair<String, String>>,
    onItemClicked: (Pair<String, String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = CardDefaults.shape
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedItemName,
                modifier = Modifier.padding(12.dp),
            )
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                onExpandedChange(false)
            }
        ) {
            items.forEach { pair ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = pair.first,
                            maxLines = 1
                        )
                    },
                    onClick = {
                        onItemClicked(pair)
                        onExpandedChange(false)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}