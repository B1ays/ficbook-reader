package ru.blays.ficbook.components.searchContent

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import ficbook_reader.compose_ui.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.components.fanficsList.FanficsListContent
import ru.blays.ficbook.platformUtils.BackHandler
import ru.blays.ficbook.reader.shared.components.fanficListComponents.declaration.FanficsListComponent
import ru.blays.ficbook.reader.shared.components.searchComponents.declaration.*
import ru.blays.ficbook.reader.shared.data.*
import ru.blays.ficbook.ui_components.CustomBottomSheetScaffold.EnhancedBottomSheetScaffold
import ru.blays.ficbook.ui_components.CustomBottomSheetScaffold.SheetValue
import ru.blays.ficbook.ui_components.CustomBottomSheetScaffold.rememberBottomSheetScaffoldState
import ru.blays.ficbook.ui_components.CustomBottomSheetScaffold.rememberSheetState
import ru.blays.ficbook.ui_components.CustomButton.CustomIconButton
import ru.blays.ficbook.ui_components.FAB.ScrollToStartFAB
import ru.blays.ficbook.ui_components.LazyItems.items
import ru.blays.ficbook.ui_components.Tabs.CustomTab
import ru.blays.ficbook.ui_components.Tabs.CustomTabIndicator
import ru.blays.ficbook.ui_components.Tabs.CustomTabRow
import ru.blays.ficbook.ui_components.dialogComponents.DialogPlatform
import ru.blays.ficbook.ui_components.spacers.HorizontalSpacer
import ru.blays.ficbook.ui_components.spacers.VerticalSpacer
import ru.blays.ficbook.utils.LocalBlurState
import ru.blays.ficbook.utils.thenIf
import ru.blays.ficbook.values.CardShape
import ru.blays.ficbook.values.DefaultPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingToolbar

@Composable
fun SearchContent(component: SearchComponent) {
    BoxWithConstraints {
        if (maxWidth > 600.dp) {
            LandscapeContent(component)
        } else {
            PortraitContent(component)
        }
    }
}

@Composable
private fun LandscapeContent(component: SearchComponent) {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Open)

    val lazyListState = rememberLazyListState()

    val blurEnabled = LocalBlurState.current
    val hazeState = remember { HazeState() }

    BackHandler(true) {
        if (drawerState.currentValue == DrawerValue.Open) {
            component.fanficsListComponent.onOutput(
                FanficsListComponent.Output.NavigateBack
            )
        } else {
            coroutineScope.launch {
                drawerState.open()
            }
        }
    }

    Scaffold(
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
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.content_description_icon_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                if (drawerState.isOpen) {
                                    drawerState.close()
                                } else {
                                    drawerState.open()
                                }
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_filter_outlined),
                            contentDescription = stringResource(Res.string.content_description_icon_filter),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.large(stringResource(Res.string.toolbar_title_search)),
                containerColor = if(blurEnabled) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.surface
                },
                collapsedElevation = if(blurEnabled) 0.dp else 4.dp,
                insets = WindowInsets.statusBars,
                modifier = Modifier.thenIf(blurEnabled) {
                    hazeChild(state = hazeState)
                }
            )
        },
        floatingActionButton = { ScrollToStartFAB(lazyListState) },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        ModalNavigationDrawer(
            gesturesEnabled = false,
            drawerContent = {
                Column(
                    modifier = Modifier
                        .background(
                            color = DrawerDefaults.standardContainerColor,
                            shape = DrawerDefaults.shape
                        )
                        .padding(top = padding.calculateTopPadding()),
                ) {
                    SearchMenuRoot(
                        component = component,
                        modifier = Modifier
                            .padding(DefaultPadding.CardDefaultPadding)
                            .fillMaxWidth(0.4F),
                    ) {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                    }
                }
            },
            scrimColor = Color.Transparent,
            drawerState = drawerState,
            modifier = Modifier.thenIf(blurEnabled) {
                haze(state = hazeState)
            }
        ) {
            FanficsListContent(
                component = component.fanficsListComponent,
                lazyListState = lazyListState,
                contentPadding = padding
            )
        }
    }
}

@Composable
private fun PortraitContent(component: SearchComponent) {
    val bottomSheetState = rememberSheetState(
        skipPartiallyExpanded = false,
        initialValue = SheetValue.Expanded
    )
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(bottomSheetState)
    val coroutineScope = rememberCoroutineScope()

    val lazyListState = rememberLazyListState()

    val blurEnabled = LocalBlurState.current
    val hazeState = remember { HazeState() }

    BackHandler(true) {
        if (bottomSheetState.currentValue == SheetValue.Expanded) {
            component.fanficsListComponent.onOutput(
                FanficsListComponent.Output.NavigateBack
            )
        } else {
            coroutineScope.launch {
                bottomSheetState.expand()
            }
        }
    }

    EnhancedBottomSheetScaffold(
        modifier = Modifier.fillMaxSize(),
        sheetPeekHeight = 0.dp,
        sheetContent = {
            SearchMenuRoot(
                component = component,
                modifier = Modifier
                    .systemBarsPadding()
                    .padding(DefaultPadding.CardDefaultPadding),
            ) {
                coroutineScope.launch {
                    bottomSheetState.partialExpand()
                }
            }
        },
        sheetDragHandle = null,
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
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.content_description_icon_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                bottomSheetState.expand()
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_filter_outlined),
                            contentDescription = stringResource(Res.string.content_description_icon_filter),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.large(stringResource(Res.string.search)),
                containerColor = if(blurEnabled) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.surface
                },
                collapsedElevation = if (blurEnabled) 0.dp else 4.dp,
                insets = WindowInsets.statusBars,
                modifier = Modifier.thenIf(blurEnabled) {
                    hazeChild(state = hazeState)
                },
            )
        },
        floatingActionButton = { ScrollToStartFAB(lazyListState) },
        floatingActionButtonPosition = FabPosition.End,
        scaffoldState = bottomSheetScaffoldState,
    ) { padding ->
        FanficsListContent(
            component = component.fanficsListComponent,
            lazyListState = lazyListState,
            contentPadding = padding,
            modifier = Modifier.thenIf(blurEnabled) {
                haze(state = hazeState)
            },
        )
    }
}

@Composable
private fun SearchMenuRoot(
    component: SearchComponent,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
) {
    val pagerState = rememberPagerState(0) { 2 }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier,
    ) {
        CustomTabRow(
            modifier = Modifier.fillMaxWidth(),
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            shape = CircleShape,
            indicator = {
                CustomTabIndicator(
                    currentPagePosition = it[pagerState.currentPage],
                    shape = CircleShape,
                    padding = 4.dp
                )
            }
        ) {
            CustomTab(
                selected = pagerState.currentPage == 0,
                selectedContentColor = MaterialTheme.colorScheme.surface,
                unselectedContentColor = MaterialTheme.colorScheme.primary,
                minHeight = 45.dp,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                }
            ) {
                Text(text = stringResource(Res.string.parameters))
            }
            CustomTab(
                selected = pagerState.currentPage == 1,
                selectedContentColor = MaterialTheme.colorScheme.surface,
                unselectedContentColor = MaterialTheme.colorScheme.primary,
                minHeight = 45.dp,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                }
            ) {
                Text(text = stringResource(Res.string.saved))
            }
        }
        VerticalCategorySpacer()
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false
        ) { page ->
            when (page) {
                0 -> {
                    SearchParamsSelector(
                        component = component,
                        onDismissRequest = onDismissRequest
                    )
                }

                1 -> {
                    SavedSearches(
                        component = component.savedSearchesComponent,
                        onBack = { scope.launch { pagerState.animateScrollToPage(0) } }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchParamsSelector(
    component: SearchComponent,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
) {
    val state by component.state.subscribeAsState()
    val scrollState = rememberScrollState()
    val fandomsState by component.searchFandomsComponent.state.subscribeAsState()

    Column(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .weight(1F)
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = {
                    component.setTitle(it)
                },
                label = {
                    Text(text = stringResource(Res.string.search_by_name))
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            component.setTitle("")
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_cancel),
                            contentDescription = stringResource(Res.string.content_description_icon_clear),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                },
                singleLine = true,
                shape = CardDefaults.shape,
                modifier = Modifier.fillMaxWidth(),
            )
            VerticalCategorySpacer()
            CheckboxWithTitle(
                checked = state.searchOriginals,
                title = stringResource(Res.string.search_originals),
                onClick = component::setSearchOriginals
            )
            VerticalCategorySpacer()
            CheckboxWithTitle(
                checked = state.searchFanfics,
                title = stringResource(Res.string.search_fanfics),
                onClick = component::setSearchFanfics
            )
            VerticalCategorySpacer()
            AnimatedVisibility(
                visible = state.searchFanfics,
                enter = expandVertically(spring()),
                exit = shrinkVertically(spring())
            ) {
                FandomsSelector(component = component.searchFandomsComponent)
                VerticalCategorySpacer()
            }

            val isPairingsSelectorVisible by remember {
                derivedStateOf {
                    state.searchFanfics && fandomsState.selectedFandoms.isNotEmpty()
                }
            }

            AnimatedVisibility(
                visible = isPairingsSelectorVisible,
                enter = expandVertically(spring()),
                exit = shrinkVertically(spring())
            ) {
                PairingSelector(component.searchCharactersComponent)
            }
            TagsSelector(
                component = component.searchTagsComponent,
            )
            VerticalCategorySpacer()
            PagesRangeSelector(
                value = state.pagesCountRange,
                onSelect = component::setPagesCountRange
            )
            VerticalCategorySpacer()
            StatusSelector(
                value = state.withStatus,
                onSelect = component::setStatus
            )
            VerticalCategorySpacer()
            RatingSelector(
                value = state.withRating,
                onSelect = component::setRating
            )
            VerticalCategorySpacer()
            DirectionSelector(
                value = state.withDirection,
                onSelect = component::setDirection
            )
            VerticalCategorySpacer()
            LikesRangeSelector(
                value = state.likesRange,
                onSelect = component::setLikesRange
            )
            VerticalCategorySpacer()
            RewardsCountSelector(
                value = state.minRewards,
                onSelect = component::setMinRewards
            )
            VerticalCategorySpacer()
            CommentsCountSelector(
                value = state.minComments,
                onSelect = component::setMinComments
            )
            VerticalCategorySpacer()
            CheckboxWithTitle(
                checked = state.onlyTranslations,
                title = stringResource(Res.string.only_translations),
                onClick = component::setOnlyTranslations
            )
            VerticalCategorySpacer()
            CheckboxWithTitle(
                checked = state.onlyPremium,
                title = stringResource(Res.string.search_selector_hot_works),
                onClick = component::setOnlyPremium
            )
            VerticalCategorySpacer()
            CheckboxWithTitle(
                checked = state.filterReaded,
                title = stringResource(Res.string.search_selector_dont_show_readed),
                onClick = component::setFilterReaded
            )
            VerticalCategorySpacer()
            SortTypeSelector(
                value = state.sort,
                onSelect = component::setSort
            )
            VerticalCategorySpacer()
        }
        BottomButtonContent(
            component = component.savedSearchesComponent,
            onSearchClick = {
                component.search()
                onDismissRequest()
            }
        )
    }
}

@Composable
private fun SavedSearches(
    modifier: Modifier = Modifier,
    component: SearchSaveComponent,
    onBack: () -> Unit,
) {
    val state by component.state.subscribeAsState()
    Column {
        LazyColumn(
            modifier = modifier,
        ) {
            items(state.saved) { shortcut ->
                SavedSearchItem(
                    shortcut = shortcut,
                    onSelect = {
                        component.select(shortcut)
                        onBack()
                    },
                    onDelete = {
                        component.delete(shortcut)
                    },
                    onUpdate = { name, description, updateParams ->
                        component.update(
                            shortcut,
                            name,
                            description,
                            updateParams
                        )
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
        Spacer(modifier = Modifier.weight(1F))
    }
}

@Composable
private fun SavedSearchItem(
    shortcut: SearchParamsEntityShortcut,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (name: String, description: String, updateParams: Boolean) -> Unit,
) {
    var editing by rememberSaveable { mutableStateOf(false) }

    var name by remember { mutableStateOf(shortcut.name) }
    var description by remember { mutableStateOf(shortcut.description) }
    var updateParams by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = if (editing) {
            {}
        } else onSelect
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = DefaultPadding.CardHorizontalPadding,
                    vertical = 5.dp
                )
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1F),
            ) {
                AnimatedContent(
                    targetState = editing,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    }
                ) {
                    if (it) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { newValue -> name = newValue },
                            label = {
                                Text(text = stringResource(Res.string.title))
                            },
                            singleLine = true,
                            shape = CardDefaults.shape,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        Text(
                            text = shortcut.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                VerticalSpacer(4.dp)
                AnimatedContent(
                    targetState = editing,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    }
                ) {
                    if (it) {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { newValue -> description = newValue },
                            label = {
                                Text(text = stringResource(Res.string.description))
                            },
                            maxLines = 5,
                            shape = CardDefaults.shape,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        if (shortcut.description.isNotEmpty()) {
                            Text(
                                text = shortcut.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                softWrap = true
                            )
                        }
                    }
                }
                AnimatedVisibility(
                    visible = editing,
                    enter = expandVertically(spring()),
                    exit = shrinkVertically(spring())
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(4.dp))
                        CheckboxWithTitle(
                            checked = updateParams,
                            title = stringResource(Res.string.search_update_parameters),
                            onClick = { updateParams = !updateParams }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row {
                            Button(
                                onClick = {
                                    onUpdate(name, description, updateParams)
                                    editing = false
                                }
                            ) {
                                Text(text = stringResource(Res.string.action_save))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = { editing = false }
                            ) {
                                Text(text = stringResource(Res.string.cancel))
                            }
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = !editing,
                enter = expandHorizontally(spring()),
                exit = shrinkHorizontally(spring())
            ) {
                Row {
                    Spacer(modifier = Modifier.width(6.dp))
                    CustomIconButton(
                        onClick = { editing = true },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        minSize = 40.dp
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_edit),
                            contentDescription = stringResource(Res.string.content_description_icon_edit),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    HorizontalSpacer(4.dp)
                    CustomIconButton(
                        onClick = onDelete,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        minSize = 40.dp
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_delete),
                            contentDescription = stringResource(Res.string.content_description_icon_delete),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FandomsSelector(
    component: SearchFandomsComponent,
) {
    val state by component.state.subscribeAsState()

    var chipExpanded by rememberSaveable { mutableStateOf(false) }

    var selectIncludedFandomDialogVisible by remember { mutableStateOf(false) }
    var selectExcludedFandomDialogVisible by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(spaceBetweenItems)
    ) {
        Text(
            text = stringResource(Res.string.search_selector_select_fandoms),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        OutlinedCardWithCornerButton(
            modifier = Modifier.animateContentSize(spring()),
            outlineColor = MaterialTheme.colorScheme.outline,
            contentColor = MaterialTheme.colorScheme.onSurface,
            iconColor = MaterialTheme.colorScheme.surface,
            label = stringResource(Res.string.search_action_add_fandom),
            icon = painterResource(Res.drawable.ic_plus),
            onClick = {
                selectIncludedFandomDialogVisible = true
            }
        ) {
            if (state.selectedFandoms.isNotEmpty()) {
                state.selectedFandoms.forEach { fandom ->
                    ItemChip(
                        modifier = Modifier
                            .padding(horizontal = DefaultPadding.CardHorizontalPadding)
                            .animateContentSize(),
                        title = fandom.title,
                        subtitle = fandom.description,
                        expanded = chipExpanded,
                        onRemove = {
                            component.selectFandom(
                                select = false,
                                fandom = fandom
                            )
                        },
                        onClick = {
                            chipExpanded = !chipExpanded
                        }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            } else {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
        OutlinedCardWithCornerButton(
            modifier = Modifier.animateContentSize(spring()),
            outlineColor = MaterialTheme.colorScheme.outline,
            contentColor = MaterialTheme.colorScheme.onSurface,
            iconColor = MaterialTheme.colorScheme.surface,
            label = stringResource(Res.string.search_action_exclude_fandom),
            icon = painterResource(Res.drawable.ic_minus),
            onClick = {
                selectExcludedFandomDialogVisible = true
            }
        ) {
            if (state.excludedFandoms.isNotEmpty()) {
                state.excludedFandoms.forEach { fandom ->
                    ItemChip(
                        modifier = Modifier
                            .padding(horizontal = DefaultPadding.CardHorizontalPadding)
                            .animateContentSize(),
                        title = fandom.title,
                        subtitle = fandom.description,
                        expanded = chipExpanded,
                        onRemove = {
                            component.excludeFandom(
                                exclude = false,
                                fandom = fandom
                            )
                        },
                        onClick = {
                            chipExpanded = !chipExpanded
                        }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            } else {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
    }

    if (selectIncludedFandomDialogVisible) {
        FindFandomDialog(
            component = component,
            onDismiss = {
                selectIncludedFandomDialogVisible = false
            },
            onSelected = { fandom ->
                component.selectFandom(
                    select = true,
                    fandom = fandom
                )
            }
        )
    }
    if (selectExcludedFandomDialogVisible) {
        FindFandomDialog(
            component = component,
            onDismiss = {
                selectExcludedFandomDialogVisible = false
            },
            onSelected = { fandom ->
                component.excludeFandom(
                    exclude = true,
                    fandom = fandom
                )
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PairingSelector(component: SearchPairingsComponent) {
    val state by component.state.subscribeAsState()

    var selectCharacterDialogVisible by remember { mutableStateOf(false) }
    Column(
        verticalArrangement = Arrangement.spacedBy(spaceBetweenItems)
    ) {
        Text(
            text = stringResource(Res.string.search_selector_select_pairings),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.buildedPairing?.characters?.isNotEmpty() == true) {
                FlowRow(
                    modifier = Modifier.padding(DefaultPadding.CardDefaultPadding),
                ) {
                    state.buildedPairing?.characters?.forEach { character ->
                        CharacterItem(
                            character = character,
                            defaultModifiers = component.defaultCharacterModifiers,
                            onModifierChange = { modifier ->
                                component.changeCharacterModifier(
                                    character = character,
                                    modifier = modifier
                                )
                            }
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(DefaultPadding.CardHorizontalPadding))
            }
            Row(
                modifier = Modifier.padding(horizontal = DefaultPadding.CardHorizontalPadding)
            ) {
                Button(
                    onClick = { selectCharacterDialogVisible = true },
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.surfaceTint
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 8.dp,
                        vertical = 3.dp
                    ),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_plus),
                        contentDescription = stringResource(Res.string.content_description_icon_plus),
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(Res.string.search_action_add_character)
                    )
                }
                AnimatedVisibility(
                    visible = state.buildedPairing != null,
                    enter = expandHorizontally(spring()) + fadeIn(),
                    exit = shrinkHorizontally(spring()) + fadeOut()
                ) {
                    Row {
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = component::clearBuildedPairing,
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.surfaceTint
                            ),
                            contentPadding = PaddingValues(
                                horizontal = 8.dp,
                                vertical = 3.dp
                            ),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_cross),
                                contentDescription = stringResource(Res.string.content_description_icon_plus),
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(Res.string.clear)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(DefaultPadding.CardHorizontalPadding))
            AnimatedVisibility(
                visible = state.buildedPairing != null,
                enter = expandVertically(spring()),
                exit = shrinkVertically(spring())
            ) {
                Column {
                    HorizontalDivider(
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.surface
                    )
                    Row {
                        Button(
                            onClick = {
                                state.buildedPairing?.let {
                                    component.selectPairing(
                                        select = true,
                                        pairing = it
                                    )
                                }
                            },
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.surfaceTint
                            ),
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .padding(vertical = 3.dp)
                                .weight(0.45F),
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_plus),
                                contentDescription = stringResource(Res.string.content_description_icon_plus),
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = stringResource(Res.string.action_add))
                        }
                        Spacer(modifier = Modifier.weight(0.10F))
                        Button(
                            onClick = {
                                state.buildedPairing?.let {
                                    component.excludePairing(
                                        exclude = true,
                                        pairing = it
                                    )
                                }
                            },
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.surfaceTint
                            ),
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .padding(vertical = 3.dp)
                                .weight(0.45F),
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_minus),
                                contentDescription = stringResource(Res.string.content_description_icon_minus),
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(Res.string.action_exclude)
                            )
                        }
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = state.selectedPairings.isNotEmpty(),
            enter = expandVertically(spring()),
            exit = shrinkVertically(spring())
        ) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline
                ),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.search_included_pairings),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                if (state.selectedPairings.isNotEmpty()) {
                    state.selectedPairings.forEach { pairing ->
                        ItemChip(
                            modifier = Modifier.padding(DefaultPadding.CardDefaultPadding),
                            title = pairing.characters.joinToString("/") {
                                "${if (it.modifier.isNotEmpty()) "${it.modifier}!" else ""}${it.name}"
                            },
                            expanded = false,
                            maxLines = Int.MAX_VALUE,
                            onRemove = {
                                component.selectPairing(
                                    select = false,
                                    pairing = pairing
                                )
                            },
                            onClick = {}
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
        AnimatedVisibility(
            visible = state.excludedPairings.isNotEmpty(),
            enter = expandVertically(spring()),
            exit = shrinkVertically(spring())
        ) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline
                ),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.search_excluded_pairings),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                if (state.excludedPairings.isNotEmpty()) {
                    state.excludedPairings.forEach { pairing ->
                        ItemChip(
                            modifier = Modifier.padding(DefaultPadding.CardDefaultPadding),
                            title = pairing.characters.joinToString("/") {
                                "${if (it.modifier.isNotEmpty()) "${it.modifier}!" else ""}${it.name}"
                            },
                            expanded = false,
                            maxLines = Int.MAX_VALUE,
                            onRemove = {
                                component.excludePairing(
                                    exclude = false,
                                    pairing = pairing
                                )
                            },
                            onClick = {}
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
        VerticalCategorySpacer()
    }
    if (selectCharacterDialogVisible) {
        SelectCharacterDialog(
            component = component,
            onDismiss = { selectCharacterDialogVisible = false }
        )
    }
}


@Composable
private fun TagsSelector(
    component: SearchTagsComponent,
) {
    val state by component.state.subscribeAsState()

    var chipExpanded by rememberSaveable { mutableStateOf(false) }

    var selectIncludedTagDialogVisible by remember { mutableStateOf(false) }
    var selectExcludedTagDialogVisible by remember { mutableStateOf(false) }
    Column(
        verticalArrangement = Arrangement.spacedBy(spaceBetweenItems)
    ) {
        Text(
            text = stringResource(Res.string.search_selector_select_tags),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        OutlinedCardWithCornerButton(
            modifier = Modifier.animateContentSize(spring()),
            outlineColor = MaterialTheme.colorScheme.outline,
            contentColor = MaterialTheme.colorScheme.onSurface,
            iconColor = MaterialTheme.colorScheme.surface,
            label = stringResource(Res.string.search_action_add_tag),
            icon = painterResource(Res.drawable.ic_plus),
            onClick = {
                selectIncludedTagDialogVisible = true
            }
        ) {
            if (state.selectedTags.isNotEmpty()) {
                state.selectedTags.forEach { tag ->
                    ItemChip(
                        modifier = Modifier
                            .padding(horizontal = DefaultPadding.CardHorizontalPadding)
                            .animateContentSize(),
                        title = tag.title,
                        subtitle = tag.description,
                        expanded = chipExpanded,
                        onRemove = {
                            component.selectTag(
                                select = false,
                                tag = tag
                            )
                        },
                        onClick = {
                            chipExpanded = !chipExpanded
                        }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            } else {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        OutlinedCardWithCornerButton(
            modifier = Modifier.animateContentSize(spring()),
            outlineColor = MaterialTheme.colorScheme.outline,
            contentColor = MaterialTheme.colorScheme.onSurface,
            iconColor = MaterialTheme.colorScheme.surface,
            label = stringResource(Res.string.search_action_exclude_tag),
            icon = painterResource(Res.drawable.ic_minus),
            onClick = {
                selectExcludedTagDialogVisible = true
            }
        ) {
            if (state.excludedTags.isNotEmpty()) {
                state.excludedTags.forEach { tag ->
                    ItemChip(
                        modifier = Modifier
                            .padding(horizontal = DefaultPadding.CardHorizontalPadding)
                            .animateContentSize(),
                        title = tag.title,
                        subtitle = tag.description,
                        expanded = chipExpanded,
                        onRemove = {
                            component.excludeTag(
                                exclude = false,
                                tag = tag
                            )
                        },
                        onClick = {
                            chipExpanded = !chipExpanded
                        }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            } else {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
        RadioButtonWithTitle(
            selected = state.behavior == SearchParams.TAGS_ANY_SELECTED,
            title = stringResource(Res.string.search_action_tagsBehavior_any),
            onClick = {
                component.changeSearchBehavior(SearchParams.TAGS_ANY_SELECTED)
            }
        )
        RadioButtonWithTitle(
            selected = state.behavior == SearchParams.TAGS_ALL_SELECTED,
            title = stringResource(Res.string.search_action_tagsBehavior_all),
            onClick = {
                component.changeSearchBehavior(SearchParams.TAGS_ALL_SELECTED)
            }
        )
    }
    if (selectIncludedTagDialogVisible) {
        FindTagDialog(
            component = component,
            onDismiss = {
                selectIncludedTagDialogVisible = false
            },
            onSelected = { tag ->
                component.selectTag(
                    select = true,
                    tag = tag
                )
            }
        )
    }
    if (selectExcludedTagDialogVisible) {
        FindTagDialog(
            component = component,
            onDismiss = {
                selectExcludedTagDialogVisible = false
            },
            onSelected = { tag ->
                component.excludeTag(
                    exclude = true,
                    tag = tag
                )
            }
        )
    }
}

@Composable
fun OutlinedCardWithCornerButton(
    modifier: Modifier = Modifier,
    outlineColor: Color = MaterialTheme.colorScheme.outline,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    iconColor: Color = MaterialTheme.colorScheme.surface,
    shape: Shape = CardDefaults.outlinedShape,
    borderWidth: Dp = 1.dp,
    label: String,
    icon: Painter,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape),
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.Transparent,
            contentColor = contentColor
        ),
        shape = shape,
        border = BorderStroke(
            width = borderWidth,
            color = outlineColor
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val buttonShape = remember {
                RoundedCornerShape(
                    bottomEnd = 16.dp
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = outlineColor,
                        shape = buttonShape
                    )
                    .clip(buttonShape)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.fillMaxSize(0.5F),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = outlineColor
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        content()
    }
}


@Composable
private fun StatusSelector(
    value: List<Int>,
    onSelect: (List<Int>) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(spaceBetweenItems)
    ) {
        Text(
            text = stringResource(Res.string.search_selector_with_status),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        CheckboxWithTitle(
            checked = SearchParams.STATUS_IN_PROGRESS in value,
            title = stringResource(Res.string.status_in_progress),
            onClick = {
                onSelect(
                    if (it) {
                        value + SearchParams.STATUS_IN_PROGRESS
                    } else {
                        value - SearchParams.STATUS_IN_PROGRESS
                    }
                )
            }
        )
        CheckboxWithTitle(
            checked = SearchParams.STATUS_COMPLETED in value,
            title = stringResource(Res.string.status_complete),
            onClick = {
                onSelect(
                    if (it) {
                        value + SearchParams.STATUS_COMPLETED
                    } else {
                        value - SearchParams.STATUS_COMPLETED
                    }
                )
            }
        )
        CheckboxWithTitle(
            checked = SearchParams.STATUS_FROZEN in value,
            title = stringResource(Res.string.status_frozen),
            onClick = {
                onSelect(
                    if (it) {
                        value + SearchParams.STATUS_FROZEN
                    } else {
                        value - SearchParams.STATUS_FROZEN
                    }
                )
            }
        )
    }
}


@Composable
private fun RatingSelector(
    value: List<Int>,
    onSelect: (List<Int>) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(spaceBetweenItems)
    ) {
        Text(
            text = stringResource(Res.string.search_selector_with_rating),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        CheckboxWithTitle(
            checked = SearchParams.RATING_G in value,
            title = stringResource(Res.string.rating_G),
            onClick = {
                onSelect(
                    if (it) {
                        value + SearchParams.RATING_G
                    } else {
                        value - SearchParams.RATING_G
                    }
                )
            }
        )
        CheckboxWithTitle(
            checked = SearchParams.RATING_PG13 in value,
            title = stringResource(Res.string.rating_PG_13),
            onClick = {
                onSelect(
                    if (it) {
                        value + SearchParams.RATING_PG13
                    } else {
                        value - SearchParams.RATING_PG13
                    }
                )
            }
        )
        CheckboxWithTitle(
            checked = SearchParams.RATING_R in value,
            title = stringResource(Res.string.rating_R),
            onClick = {
                onSelect(
                    if (it) {
                        value + SearchParams.RATING_R
                    } else {
                        value - SearchParams.RATING_R
                    }
                )
            }
        )
        CheckboxWithTitle(
            checked = SearchParams.RATING_NC17 in value,
            title = stringResource(Res.string.rating_NC_17),
            onClick = {
                onSelect(
                    if (it) {
                        value + SearchParams.RATING_NC17
                    } else {
                        value - SearchParams.RATING_NC17
                    }
                )
            }
        )
        CheckboxWithTitle(
            checked = SearchParams.RATING_NC21 in value,
            title = stringResource(Res.string.rating_NC_21),
            onClick = {
                onSelect(
                    if (it) {
                        value + SearchParams.RATING_NC21
                    } else {
                        value - SearchParams.RATING_NC21
                    }
                )
            }
        )
    }
}


@Composable
private fun DirectionSelector(
    value: List<Int>,
    onSelect: (List<Int>) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(spaceBetweenItems)
    ) {
        Text(
            text = stringResource(Res.string.search_selector_direction),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        CheckboxWithTitle(
            checked = SearchParams.DIRECTION_GEN in value,
            title = stringResource(Res.string.direction_gen),
            onClick = {
                onSelect(
                    if (it) {
                        value + SearchParams.DIRECTION_GEN
                    } else {
                        value - SearchParams.DIRECTION_GEN
                    }
                )
            }
        )
        CheckboxWithTitle(
            checked = SearchParams.DIRECTION_HET in value,
            title = stringResource(Res.string.direction_het),
            onClick = {
                onSelect(
                    if (it) {
                        value + SearchParams.DIRECTION_HET
                    } else {
                        value - SearchParams.DIRECTION_HET
                    }
                )
            }
        )
        CheckboxWithTitle(
            checked = SearchParams.DIRECTION_SLASH in value,
            title = stringResource(Res.string.direction_slash),
            onClick = {
                onSelect(
                    if (it) {
                        value + SearchParams.DIRECTION_SLASH
                    } else {
                        value - SearchParams.DIRECTION_SLASH
                    }
                )
            }
        )
        CheckboxWithTitle(
            checked = SearchParams.DIRECTION_FEMSLASH in value,
            title = stringResource(Res.string.direction_femslash),
            onClick = {
                onSelect(
                    if (it) {
                        value + SearchParams.DIRECTION_FEMSLASH
                    } else {
                        value - SearchParams.DIRECTION_FEMSLASH
                    }
                )
            }
        )
        CheckboxWithTitle(
            checked = SearchParams.DIRECTION_OTHER in value,
            title = stringResource(Res.string.direction_other),
            onClick = {
                onSelect(
                    if (it) {
                        value + SearchParams.DIRECTION_OTHER
                    } else {
                        value - SearchParams.DIRECTION_OTHER
                    }
                )
            }
        )
        CheckboxWithTitle(
            checked = SearchParams.DIRECTION_MIXED in value,
            title = stringResource(Res.string.direction_mixed),
            onClick = {
                onSelect(
                    if (it) {
                        value + SearchParams.DIRECTION_MIXED
                    } else {
                        value - SearchParams.DIRECTION_MIXED
                    }
                )
            }
        )
        CheckboxWithTitle(
            checked = SearchParams.DIRECTION_ARTICLE in value,
            title = stringResource(Res.string.direction_article),
            onClick = {
                onSelect(
                    if (it) {
                        value + SearchParams.DIRECTION_ARTICLE
                    } else {
                        value - SearchParams.DIRECTION_ARTICLE
                    }
                )
            }
        )
    }
}

@Composable
fun PagesRangeSelector(
    value: IntRangeSimple,
    onSelect: (IntRangeSimple) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(spaceBetweenItems)
    ) {
        Text(
            text = stringResource(Res.string.search_selector_pages_count),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        RangeSelector(
            value = value,
            onSelect = onSelect
        )
    }
}


@Composable
fun LikesRangeSelector(
    value: IntRangeSimple,
    onSelect: (IntRangeSimple) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(spaceBetweenItems)
    ) {
        Text(
            text = stringResource(Res.string.search_selector_likes_count),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        RangeSelector(
            value = value,
            onSelect = onSelect
        )
    }
}


@Composable
fun RewardsCountSelector(
    value: Int,
    onSelect: (Int) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(spaceBetweenItems)
    ) {
        Text(
            text = stringResource(Res.string.search_selector_rewards_count),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        OutlinedTextField(
            value = if (value == 0) "" else "$value",
            onValueChange = {
                onSelect(it.toIntOrNull() ?: 0)
            },
            label = {
                Text(text = stringResource(Res.string.from))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = CardDefaults.shape,
            modifier = Modifier.fillMaxWidth(0.4F)
        )
    }
}

@Composable
fun CommentsCountSelector(
    value: Int,
    onSelect: (Int) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(spaceBetweenItems)
    ) {
        Text(
            text = stringResource(Res.string.search_selector_comments_count),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        OutlinedTextField(
            value = if (value == 0) "" else "$value",
            onValueChange = {
                onSelect(it.toIntOrNull() ?: 0)
            },
            label = {
                Text(text = stringResource(Res.string.from))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = CardDefaults.shape,
            modifier = Modifier.fillMaxWidth(0.4F)
        )
    }
}

@Composable
fun SortTypeSelector(
    value: Int,
    onSelect: (Int) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(spaceBetweenItems)
    ) {
        Text(
            text = stringResource(Res.string.search_selector_sort_type),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.SORT_BY_LIKES_COUNT,
            title = stringResource(Res.string.sort_by_likes),
            onClick = {
                onSelect(SearchParams.SORT_BY_LIKES_COUNT)
            }
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.SORT_BY_COMMENTS_COUNT,
            title = stringResource(Res.string.sort_by_comments),
            onClick = {
                onSelect(SearchParams.SORT_BY_COMMENTS_COUNT)
            }
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.SORT_BY_DATE_FROM_NEW,
            title = stringResource(Res.string.sort_by_date_from_new),
            onClick = {
                onSelect(SearchParams.SORT_BY_DATE_FROM_NEW)
            }
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.SORT_BY_DATE_FROM_OLD,
            title = stringResource(Res.string.sort_by_date_from_old),
            onClick = {
                onSelect(SearchParams.SORT_BY_DATE_FROM_OLD)
            }
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.SORT_BY_PAGES_COUNT,
            title = stringResource(Res.string.sort_by_pages),
            onClick = {
                onSelect(SearchParams.SORT_BY_PAGES_COUNT)
            }
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.SORT_BY_REWARDS_COUNT,
            title = stringResource(Res.string.sort_by_rewards),
            onClick = {
                onSelect(SearchParams.SORT_BY_REWARDS_COUNT)
            }
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.SORT_20_RANDOM,
            title = stringResource(Res.string.sort_20_random),
            onClick = {
                onSelect(SearchParams.SORT_20_RANDOM)
            }
        )
    }
}


@Composable
fun RangeSelector(
    value: IntRangeSimple,
    onSelect: (IntRangeSimple) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = if (value.start == 0) "" else "${value.start}",
            onValueChange = {
                println(it)
                onSelect(
                    value.copy(
                        start = it.toIntOrNull() ?: 0
                    )
                )
            },
            label = {
                Text(text = stringResource(Res.string.minimum))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = CardDefaults.shape,
            modifier = Modifier.weight(0.4F)
        )
        OutlinedTextField(
            value = if (value.end == 0) "" else "${value.end}",
            onValueChange = {
                onSelect(
                    value.copy(
                        end = it.toIntOrNull() ?: 0
                    )
                )
            },
            label = {
                Text(text = stringResource(Res.string.maximum))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = CardDefaults.shape,
            modifier = Modifier.weight(0.4F)
        )
    }
}


@Composable
private fun FindFandomDialog(
    component: SearchFandomsComponent,
    onDismiss: () -> Unit,
    onSelected: (SearchedFandomModel) -> Unit,
) {
    val state by component.state.subscribeAsState()
    DialogPlatform(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.6F)
    ) {
        Card {
            Column(
                modifier = Modifier.padding(6.dp)
            ) {
                OutlinedTextField(
                    value = state.searchedName,
                    onValueChange = component::changeSearchedName,
                    label = {
                        Text(text = stringResource(Res.string.search_by_name))
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = component::clear
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_cancel),
                                contentDescription = stringResource(Res.string.content_description_icon_clear),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    },
                    singleLine = true,
                    shape = CardDefaults.shape,
                    modifier = Modifier.fillMaxWidth(),
                )
                VerticalCategorySpacer()
                LazyColumn(
                    modifier = Modifier.weight(1F),
                ) {
                    items(state.searchedFandoms) { fandom ->
                        SearchedFandomItem(
                            fandom = fandom,
                            onSelected = {
                                onSelected(fandom)
                                onDismiss()
                            }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss
                    ) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            }
        }
    }
}


@Composable
private fun FindTagDialog(
    component: SearchTagsComponent,
    onDismiss: () -> Unit,
    onSelected: (SearchedTagModel) -> Unit,
) {
    val state by component.state.subscribeAsState()
    DialogPlatform(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.6F)
    ) {
        Card {
            Column(
                modifier = Modifier.padding(6.dp)
            ) {
                OutlinedTextField(
                    value = state.searchedName,
                    onValueChange = component::changeSearchedName,
                    label = {
                        Text(text = stringResource(Res.string.search_by_name))
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = component::clear
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_cancel),
                                contentDescription = stringResource(Res.string.content_description_icon_clear),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    },
                    singleLine = true,
                    shape = CardDefaults.shape,
                    modifier = Modifier.fillMaxWidth(),
                )
                VerticalCategorySpacer()
                LazyColumn(
                    modifier = Modifier.weight(1F),
                ) {
                    items(state.searchedTags) { fandom ->
                        SearchedTagItem(
                            tag = fandom,
                            onSelected = {
                                onSelected(fandom)
                                onDismiss()
                            }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss
                    ) {
                        Text(text = stringResource(Res.string.cancel))
                    }
                }
            }
        }
    }
}


@Composable
fun SelectCharacterDialog(
    component: SearchPairingsComponent,
    onDismiss: () -> Unit,
) {
    val state by component.state.subscribeAsState()
    var searchedName by remember { mutableStateOf("") }

    val filteredList = remember(searchedName) {
        if (searchedName.isEmpty()) {
            state.searchedCharacters
        } else {
            state.searchedCharacters.map { group ->
                group.copy(
                    characters = group.characters.filter { character ->
                        character.name.contains(searchedName, ignoreCase = true) ||
                                character.aliases.any { it.contains(searchedName, ignoreCase = true) }
                    }
                )
            }
        }
    }

    DialogPlatform(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.6F)
    ) {
        Card {
            Column(
                modifier = Modifier.padding(6.dp)
            ) {
                OutlinedTextField(
                    value = searchedName,
                    onValueChange = { searchedName = it },
                    label = {
                        Text(text = stringResource(Res.string.search_by_name))
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { searchedName = "" }
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_cancel),
                                contentDescription = stringResource(Res.string.content_description_icon_clear),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    },
                    singleLine = true,
                    shape = CardDefaults.shape,
                    modifier = Modifier.fillMaxWidth(),
                )
                VerticalCategorySpacer()

                val lazyColumnState = rememberLazyListState()

                val ranges = remember(filteredList) {
                    var previousGroupEnd = 0
                    filteredList.map { group ->
                        val groupEnd = previousGroupEnd + group.characters.lastIndex
                        val range = previousGroupEnd..groupEnd
                        previousGroupEnd = groupEnd
                        return@map range
                    }
                }

                val categoryTitle by remember {
                    derivedStateOf {
                        filteredList.getOrNull(
                            index = ranges.indexOfFirst { lazyColumnState.firstVisibleItemIndex in it }
                        )?.fandomName ?: ""
                    }
                }

                LazyColumn(
                    state = lazyColumnState,
                    modifier = Modifier.weight(1F),
                ) {
                    stickyHeader {
                        Text(
                            text = categoryTitle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(6.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    filteredList.forEach { group ->
                        items(group.characters) { character ->
                            SearchedCharacterItem(
                                character = character,
                                onClick = {
                                    component.addCharacterToPairing(character)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss
                    ) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchedFandomItem(
    fandom: SearchedFandomModel,
    onSelected: (SearchedFandomModel) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(6.dp)
            .clickable {
                onSelected(fandom)
            },
    ) {
        Text(
            text = "${fandom.title} (${fandom.fanficsCount})",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = fandom.description,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun SearchedTagItem(
    tag: SearchedTagModel,
    onSelected: (SearchedTagModel) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(6.dp)
            .clickable {
                onSelected(tag)
            },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${tag.title} (${tag.usageCount})",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium
            )
            if (tag.isAdult) {
                Spacer(modifier = Modifier.width(spaceBetweenItems))
                Icon(
                    painter = painterResource(Res.drawable.ic_18),
                    contentDescription = stringResource(Res.string.content_description_icon_18),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tag.description,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SearchedCharacterItem(
    character: SearchedCharacterModel,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(6.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Text(
            text = character.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (character.aliases.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = character.aliases.joinToString(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
private fun CharacterItem(
    character: SearchedPairingModel.Character,
    defaultModifiers: Array<String>,
    onModifierChange: (modifier: String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.padding(vertical = 3.dp),
    ) {
        Row(
            modifier = Modifier
                .height(30.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.small
                )
                .clip(MaterialTheme.shapes.small)
                .weight(1F, false),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {
                    expanded = it
                }
            ) {
                Row(
                    modifier = Modifier
                        .menuAnchor()
                        .padding(horizontal = 4.dp)
                        .animateContentSize(spring()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (character.modifier.isNotEmpty()) {
                        Text(
                            text = character.modifier,
                            style = MaterialTheme.typography.labelLarge
                        )
                    } else {
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        painter = painterResource(Res.drawable.ic_arrow_down),
                        contentDescription = stringResource(Res.string.content_description_icon_down),
                        modifier = Modifier.size(20.dp),
                    )
                }
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                    },
                    modifier = Modifier
                        .exposedDropdownSize(false)
                        .fillMaxWidth(),
                ) {
                    DropdownMenuItem(
                        onClick = {
                            onModifierChange("")
                            expanded = false
                        },
                        text = {}
                    )
                    defaultModifiers.forEach {
                        DropdownMenuItem(
                            onClick = {
                                onModifierChange(it)
                                expanded = false
                            },
                            text = {
                                Text(text = "$it!")
                            }
                        )
                    }
                    var textFieldValue by remember { mutableStateOf("") }
                    DropdownMenuItem(
                        onClick = {
                            onModifierChange(textFieldValue)
                            expanded = false
                        },
                        text = {
                            TextField(
                                value = textFieldValue,
                                onValueChange = {
                                    textFieldValue = it
                                },
                                placeholder = {
                                    Text(stringResource(Res.string.search_action_add_your))
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            onModifierChange(textFieldValue)
                                            expanded = false
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_check),
                                            contentDescription = stringResource(Res.string.content_description_icon_check),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                singleLine = true,
                                shape = RectangleShape,
                                colors = TextFieldDefaults.colors(
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    )
                }
            }
            VerticalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant,
                thickness = 2.dp
            )
            Text(
                text = character.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.small
                )
                .clip(MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "/",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.surfaceTint
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
    }
}


@Composable
private fun ItemChip(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    expanded: Boolean,
    maxLines: Int = 1,
    onRemove: () -> Unit,
    onClick: () -> Unit,
) {
    InputChip(
        modifier = modifier,
        selected = false,
        onClick = onClick,
        label = {
            Column {
                Text(
                    text = title,
                    maxLines = maxLines,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium
                )
                if (expanded && subtitle != null) {
                    Text(
                        text = subtitle,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        },
        trailingIcon = {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_cancel),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    )
}

@Composable
private fun RadioButtonWithTitle(
    modifier: Modifier = Modifier,
    selected: Boolean,
    title: String,
    onClick: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .padding(3.dp)
            .toggleable(
                value = selected,
                onValueChange = onClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            modifier = Modifier.padding(end = 10.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1F),
        )
    }
}

@Composable
private fun CheckboxWithTitle(
    modifier: Modifier = Modifier,
    checked: Boolean,
    title: String,
    onClick: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .padding(3.dp)
            .toggleable(
                value = checked,
                onValueChange = onClick
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1F),
        )
        Checkbox(
            checked = checked,
            onCheckedChange = null
        )
    }
}


@Composable
private fun BottomButtonContent(
    component: SearchSaveComponent,
    onSearchClick: () -> Unit,
) {
    var saveMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    AnimatedVisibility(
        visible = saveMenuExpanded,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Column(
            modifier = Modifier.padding(vertical = 3.dp),
        ) {
            Text(
                text = stringResource(Res.string.search_action_save_search),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(text = stringResource(Res.string.title)) },
                singleLine = true,
                shape = CardDefaults.shape,
                modifier = Modifier.fillMaxWidth(),
            )
            VerticalCategorySpacer()
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(text = stringResource(Res.string.description)) },
                maxLines = 5,
                shape = CardDefaults.shape,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(spring()),
            )
        }
    }
    Row(
        modifier = Modifier.padding(vertical = 3.dp)
    ) {
        Button(
            onClick = {
                if (saveMenuExpanded) {
                    component.save(name, description)
                    saveMenuExpanded = false
                } else {
                    onSearchClick()
                }
            },
            shape = CardShape.CardMid,
            enabled = if (saveMenuExpanded) name.isNotEmpty() else true,
            modifier = Modifier
                .height(50.dp)
                .weight(1F)
        ) {
            AnimatedContent(
                targetState = saveMenuExpanded,
                transitionSpec = {
                    fadeIn() + scaleIn() togetherWith
                            fadeOut() + scaleOut()
                },
                contentAlignment = Alignment.Center
            ) { expanded ->
                if (expanded) {
                    Text(
                        text = stringResource(Res.string.action_save),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.action_search),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Button(
            onClick = {
                saveMenuExpanded = !saveMenuExpanded
            },
            shape = CardShape.CardMid,
            contentPadding = PaddingValues(2.dp),
            modifier = Modifier.size(50.dp),
        ) {
            AnimatedContent(
                targetState = saveMenuExpanded,
                transitionSpec = {
                    fadeIn() + scaleIn() togetherWith
                            fadeOut() + scaleOut()
                },
                contentAlignment = Alignment.Center
            ) { expanded ->
                if (expanded) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_cancel),
                        contentDescription = stringResource(Res.string.content_description_icon_star),
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(Res.drawable.ic_star_outlined),
                        contentDescription = stringResource(Res.string.content_description_icon_star),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VerticalCategorySpacer() = VerticalSpacer(spaceBetweenCategories)

private val spaceBetweenCategories = 8.dp
private val spaceBetweenItems = 6.dp