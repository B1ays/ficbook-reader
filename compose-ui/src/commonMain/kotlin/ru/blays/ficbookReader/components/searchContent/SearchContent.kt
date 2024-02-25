package ru.blays.ficbookReader.components.searchContent

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.myapplication.compose.Res
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import io.github.skeptick.libres.compose.painterResource
import kotlinx.coroutines.launch
import ru.blays.ficbookReader.components.fanficsList.FanficsListContent
import ru.blays.ficbookReader.platformUtils.BackHandler
import ru.blays.ficbookReader.shared.data.dto.*
import ru.blays.ficbookReader.shared.ui.fanficListComponents.declaration.FanficsListComponent
import ru.blays.ficbookReader.shared.ui.searchComponents.declaration.SearchComponent
import ru.blays.ficbookReader.shared.ui.searchComponents.declaration.SearchFandomsComponent
import ru.blays.ficbookReader.shared.ui.searchComponents.declaration.SearchPairingsComponent
import ru.blays.ficbookReader.shared.ui.searchComponents.declaration.SearchTagsComponent
import ru.blays.ficbookReader.ui_components.CustomBottomSheetScaffold.BottomSheetScaffold
import ru.blays.ficbookReader.ui_components.CustomBottomSheetScaffold.SheetValue
import ru.blays.ficbookReader.ui_components.CustomBottomSheetScaffold.rememberBottomSheetScaffoldState
import ru.blays.ficbookReader.ui_components.CustomBottomSheetScaffold.rememberSheetState
import ru.blays.ficbookReader.ui_components.LazyItems.items
import ru.blays.ficbookReader.utils.LocalGlassEffectConfig
import ru.blays.ficbookReader.utils.thenIf
import ru.blays.ficbookReader.values.CardShape
import ru.blays.ficbookReader.values.DefaultPadding
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

    val glassEffectConfig = LocalGlassEffectConfig.current
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
                            painter = painterResource(Res.image.ic_arrow_back),
                            contentDescription = "Стрелка назад"
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
                            painter = painterResource(Res.image.ic_filter_outlined),
                            contentDescription = "Иконка фильтр",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.large("Поиск"),
                containerColor = if (glassEffectConfig.blurEnabled) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.surface
                },
                collapsedElevation = if (glassEffectConfig.blurEnabled) 0.dp else 4.dp,
                insets = WindowInsets.statusBars,
                modifier = Modifier.thenIf(glassEffectConfig.blurEnabled) {
                    hazeChild(
                        state = hazeState,
                        style = glassEffectConfig.style
                    )
                }
            )
        }
    ) { padding ->
        ModalNavigationDrawer(
            gesturesEnabled = false,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.padding(
                        top = padding.calculateTopPadding()
                    ),
                ) {
                    SearchMenu(
                        component = component,
                        modifier = Modifier
                            .padding(DefaultPadding.CardDefaultPadding)
                            .width(450.dp),
                    )
                }
            },
            drawerState = drawerState,
            modifier = Modifier.thenIf(glassEffectConfig.blurEnabled) {
                haze(state = hazeState)
            }
        ) {
            FanficsListContent(
                component = component.fanficsListComponent,
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

    val glassEffectConfig = LocalGlassEffectConfig.current
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

    BottomSheetScaffold(
        modifier = Modifier.fillMaxSize(),
        sheetPeekHeight = 0.dp,
        sheetContent = {
            SearchMenu(
                component = component,
                modifier = Modifier.padding(DefaultPadding.CardDefaultPadding),
            ) {
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
                                bottomSheetState.expand()
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.image.ic_filter_outlined),
                            contentDescription = "Иконка фильтр",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.large("Поиск"),
                containerColor = if (glassEffectConfig.blurEnabled) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.surface
                },
                collapsedElevation = if (glassEffectConfig.blurEnabled) 0.dp else 4.dp,
                insets = WindowInsets.statusBars,
                modifier = Modifier.thenIf(glassEffectConfig.blurEnabled) {
                    hazeChild(
                        state = hazeState,
                        style = glassEffectConfig.style
                    )
                },
            )
        },
        scaffoldState = bottomSheetScaffoldState,
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
private fun SearchMenu(
    component: SearchComponent,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
) {
    val state by component.state.subscribeAsState()
    val scrollState = rememberScrollState()
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
                    Text(text = "Поиск по названию")
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            component.setTitle("")
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
                shape = CardDefaults.shape,
                modifier = Modifier.fillMaxWidth(),
            )
            VerticalCategorySpacer()
            FandomFilter(
                value = state.fandomsFilter,
                onValueChange = component::setFandomsFilter
            )
            VerticalCategorySpacer()
            AnimatedVisibility(
                visible = state.fandomsFilter == SearchParams.FANDOM_FILTER_CATEGORY,
                enter = expandVertically(spring()),
                exit = shrinkVertically(spring())
            ) {
                FandomGroupSelector(
                    value = state.fandomsGroup,
                    onValueChange = component::setFandomsGroup
                )
                VerticalCategorySpacer()
            }
            AnimatedVisibility(
                visible = state.fandomsFilter == SearchParams.FANDOM_FILTER_CATEGORY ||
                    state.fandomsFilter == SearchParams.FANDOM_FILTER_CONCRETE,
                enter = expandVertically(spring()),
                exit = shrinkVertically(spring())
            ) {
                FandomsSelector(
                    component = component.searchFandomsComponent,
                    canIncludeFandoms = state.fandomsFilter == SearchParams.FANDOM_FILTER_CONCRETE
                )
                VerticalCategorySpacer()
            }
            AnimatedVisibility(
                visible = state.fandomsFilter == SearchParams.FANDOM_FILTER_CONCRETE,
                enter = expandVertically(spring()),
                exit = shrinkVertically(spring())
            ) {
                PairingSelector(component.searchCharactersComponent)
                VerticalCategorySpacer()
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
            TranslateSelector(
                value = state.translate,
                onSelect = component::setTranslate
            )
            VerticalCategorySpacer()
            CheckboxWithTitle(
                checked = state.onlyPremium,
                title = "Фанфики из раздела «Горячие работы»",
                onClick = component::setOnlyPremium
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
            CheckboxWithTitle(
                checked = state.filterReaded,
                title = "Не показывать прочитанное",
                onClick = component::setFilterReaded
            )
            VerticalCategorySpacer()
            SortTypeSelector(
                value = state.sort,
                onSelect = component::setSort
            )
            VerticalCategorySpacer()
        }
        Button(
            onClick = {
                component.search()
                onDismissRequest()
            },
            shape = CardShape.CardMid,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(DefaultPadding.CardDefaultPadding),
        ) {
            Text(
                text = "Найти",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FandomFilter(
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(spaceBetweenItems)
    ) {
        RadioButtonWithTitle(
            selected = value == SearchParams.FANDOM_FILTER_ALL,
            title = "Все",
            onClick = {
                onValueChange(SearchParams.FANDOM_FILTER_ALL)
            }
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.FANDOM_FILTER_ORIGINALS,
            title = "Ориджиналы",
            onClick = {
                onValueChange(SearchParams.FANDOM_FILTER_ORIGINALS)
            }
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.FANDOM_FILTER_CATEGORY,
            title = "Все фанфики в группе фэндомов",
            onClick = {
                onValueChange(SearchParams.FANDOM_FILTER_CATEGORY)
            }
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.FANDOM_FILTER_CONCRETE,
            title = "Фанфики по конкретным фэндомам",
            onClick = {
                onValueChange(SearchParams.FANDOM_FILTER_CONCRETE)
            }
        )
    }
}

@Composable
fun FandomGroupSelector(
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(spaceBetweenItems)
    ) {
        Text(
            text = "Группа фэндомов",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.FANDOM_GROUP_ANIME_AND_MANGA,
            title = "Аниме и манга",
            onClick = {
                onValueChange(SearchParams.FANDOM_GROUP_ANIME_AND_MANGA)
            }
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.FANDOM_GROUP_BOOKS,
            title = "Книги",
            onClick = {
                onValueChange(SearchParams.FANDOM_GROUP_BOOKS)
            }
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.FANDOM_GROUP_CARTOONS,
            title = "Мультфильмы",
            onClick = {
                onValueChange(SearchParams.FANDOM_GROUP_CARTOONS)
            }
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.FANDOM_GROUP_GAMES,
            title = "Игры",
            onClick = {
                onValueChange(SearchParams.FANDOM_GROUP_GAMES)
            }
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.FANDOM_GROUP_MOVIES,
            title = "Фильмы и сериалы",
            onClick = {
                onValueChange(SearchParams.FANDOM_GROUP_MOVIES)
            }
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.FANDOM_GROUP_OTHER,
            title = "Другие",
            onClick = {
                onValueChange(SearchParams.FANDOM_GROUP_OTHER)
            }
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.FANDOM_GROUP_RPF,
            title = "Известные люди (RPF)",
            onClick = {
                onValueChange(SearchParams.FANDOM_GROUP_RPF)
            }
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.FANDOM_GROUP_COMICS,
            title = "Комиксы",
            onClick = {
                onValueChange(SearchParams.FANDOM_GROUP_COMICS)
            }
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.FANDOM_GROUP_MUSICALS,
            title = "Мюзиклы",
            onClick = {
                onValueChange(SearchParams.FANDOM_GROUP_MUSICALS)
            }
        )
    }
}


@Composable
private fun FandomsSelector(
    component: SearchFandomsComponent,
    canIncludeFandoms: Boolean
) {
    val state by component.state.subscribeAsState()

    var chipExpanded by rememberSaveable { mutableStateOf(false) }

    var selectIncludedFandomDialogVisible by remember { mutableStateOf(false) }
    var selectExcludedFandomDialogVisible by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(spaceBetweenItems)
    ) {
        Text(
            text = "Выбрать фэндомы",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (canIncludeFandoms) {
            OutlinedCardWithCornerButton(
                modifier = Modifier.animateContentSize(spring()),
                outlineColor = MaterialTheme.colorScheme.outline,
                contentColor = MaterialTheme.colorScheme.onSurface,
                iconColor = MaterialTheme.colorScheme.surface,
                label = "Добавить фэндом",
                icon = painterResource(Res.image.ic_plus),
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
        }

        OutlinedCardWithCornerButton(
            modifier = Modifier.animateContentSize(spring()),
            outlineColor = MaterialTheme.colorScheme.outline,
            contentColor = MaterialTheme.colorScheme.onSurface,
            iconColor = MaterialTheme.colorScheme.surface,
            label = "Исключить фэндом",
            icon = painterResource(Res.image.ic_minus),
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
            text = "Выбрать пейринги или персонажей",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            if(state.buildedPairing?.characters?.isNotEmpty() == true) {
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
                        painter = painterResource(Res.image.ic_plus),
                        contentDescription = "Иконка плюс",
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Добавить персонажа"
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
                                painter = painterResource(Res.image.ic_cross),
                                contentDescription = "Иконка плюс",
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Очистить"
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
                                painter = painterResource(Res.image.ic_plus),
                                contentDescription = "Иконка плюс",
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Добавить")
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
                                painter = painterResource(Res.image.ic_minus),
                                contentDescription = "Иконка минус",
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Исключить"
                            )
                        }
                    }
                }
            }
        }
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
                text = "Пейринги",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            if (state.selectedPairings.isNotEmpty()) {
                state.selectedPairings.forEach { pairing ->
                    ItemChip(
                        modifier = Modifier.padding(DefaultPadding.CardDefaultPadding),
                        title = pairing.characters.joinToString("/") {
                            "${if(it.modifier.isNotEmpty()) "${it.modifier}!" else ""}${it.name}"
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
                text = "Исключённые пейринги",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            if(state.excludedPairings.isNotEmpty()) {
                state.excludedPairings.forEach { pairing ->
                    ItemChip(
                        modifier = Modifier.padding(DefaultPadding.CardDefaultPadding),
                        title = pairing.characters.joinToString("/") {
                            "${if(it.modifier.isNotEmpty()) "${it.modifier}!" else ""}${it.name}"
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
    if(selectCharacterDialogVisible) {
        SelectCharacterDialog(
            component = component,
            onDismiss = { selectCharacterDialogVisible = false }
        )
    }
}

@Composable
private fun TagsSelector(
    component: SearchTagsComponent
) {
    val state by component.state.subscribeAsState()

    var chipExpanded by rememberSaveable { mutableStateOf(false) }

    var selectIncludedTagDialogVisible by remember { mutableStateOf(false) }
    var selectExcludedTagDialogVisible by remember { mutableStateOf(false) }
    Column(
        verticalArrangement = Arrangement.spacedBy(spaceBetweenItems)
    ) {
        Text(
            text = "Выбрать метки",
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
            label = "Добавить метку",
            icon = painterResource(Res.image.ic_plus),
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
            label = "Исключить метку",
            icon = painterResource(Res.image.ic_minus),
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
        Spacer(modifier = Modifier.height(6.dp))
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
    content: @Composable ColumnScope.() -> Unit
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
    onSelect: (List<Int>) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(spaceBetweenItems)
    ) {
        Text(
            text = "Со статусом",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        CheckboxWithTitle(
            checked = SearchParams.STATUS_IN_PROGRESS in value,
            title = "В процессе",
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
            title = "Завершён",
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
            title = "Заморожен",
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
    onSelect: (List<Int>) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(spaceBetweenItems)
    ) {
        Text(
            text = "С рейтингом",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        CheckboxWithTitle(
            checked = SearchParams.RATING_G in value,
            title = "G",
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
            title = "PG-13",
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
            title = "R",
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
            title = "NC-17",
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
            title = "NC-21",
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
    onSelect: (List<Int>) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(spaceBetweenItems)
    ) {
        Text(
            text = "Одной из направленностей",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        CheckboxWithTitle(
            checked = SearchParams.DIRECTION_GEN in value,
            title = "Джен",
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
            title = "Гет",
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
            title = "Слэш",
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
            title = "Фемслэш",
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
            title = "Другие виды отношений",
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
            title = "Смешанная",
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
            title = "Статья",
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
private fun TranslateSelector(
    value: Int,
    onSelect: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(spaceBetweenItems)
    ) {
        Text(
            text = "Перевод или оригинальный текст",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.TRANSLATE_DOESNT_MATTER,
            title = "Всё равно",
            onClick = {
                onSelect(SearchParams.TRANSLATE_DOESNT_MATTER)
            }
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.TRANSLATE_YES,
            title = "Перевод с иностранного языка",
            onClick = {
                onSelect(SearchParams.TRANSLATE_YES)
            }
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.TRANSLATE_NO,
            title = "Оригинальный текст",
            onClick = {
                onSelect(SearchParams.TRANSLATE_NO)
            }
        )
    }
}

@Composable
fun PagesRangeSelector(
    value: IntRangeSimple,
    onSelect: (IntRangeSimple) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(spaceBetweenItems)
    ) {
        Text(
            text = "Количество страниц",
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
    onSelect: (IntRangeSimple) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(spaceBetweenItems)
    ) {
        Text(
            text = "Количество лайков",
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
    onSelect: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(spaceBetweenItems)
    ) {
        Text(
            text = "Количество наград",
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
                Text(text = "От")
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
    onSelect: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(spaceBetweenItems)
    ) {
        Text(
            text = "Отсортировать результат",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.SORT_BY_LIKES_COUNT,
            title = "по оценкам читателей",
            onClick = {
                onSelect(SearchParams.SORT_BY_LIKES_COUNT)
            }
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.SORT_BY_COMMENTS_COUNT,
            title = "по количеству отзывов",
            onClick = {
                onSelect(SearchParams.SORT_BY_COMMENTS_COUNT)
            }
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.SORT_BY_DATE_FROM_NEW,
            title = "по дате обновления (от новых к старым)",
            onClick = {
                onSelect(SearchParams.SORT_BY_DATE_FROM_NEW)
            }
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.SORT_BY_DATE_FROM_OLD,
            title = "по дате обновления (от старых к новым)",
            onClick = {
                onSelect(SearchParams.SORT_BY_DATE_FROM_OLD)
            }
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.SORT_BY_PAGES_COUNT,
            title = "по количеству страниц",
            onClick = {
                onSelect(SearchParams.SORT_BY_PAGES_COUNT)
            }
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.SORT_BY_REWARDS_COUNT,
            title = "по количеству наград",
            onClick = {
                onSelect(SearchParams.SORT_BY_REWARDS_COUNT)
            }
        )
        RadioButtonWithTitle(
            selected = value == SearchParams.SORT_20_RANDOM,
            title = "20 случайных работ",
            onClick = {
                onSelect(SearchParams.SORT_20_RANDOM)
            }
        )
    }
}

@Composable
fun RangeSelector(
    value: IntRangeSimple,
    onSelect: (IntRangeSimple) -> Unit
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
                Text(text = "Минимум")
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
                Text(text = "Максимум")
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
    onSelected: (SearchedFandomModel) -> Unit
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
                        Text(text = "Поиск по названию")
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = component::clear
                        ) {
                            Icon(
                                painter = painterResource(Res.image.ic_cancel),
                                contentDescription = "Очистить поиск",
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
                        Text("Отмена")
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
    onSelected: (SearchedTagModel) -> Unit
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
                        Text(text = "Поиск по названию")
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = component::clear
                        ) {
                            Icon(
                                painter = painterResource(Res.image.ic_cancel),
                                contentDescription = "Очистить поиск",
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
                        Text("Отмена")
                    }
                }
            }
        }
    }
}

@Composable
fun SelectCharacterDialog(
    component: SearchPairingsComponent,
    onDismiss: () -> Unit
) {
    val state by component.state.subscribeAsState()
    var searchedName by remember { mutableStateOf("") }

    val filteredList = remember(searchedName) {
        if(searchedName.isEmpty()) state.searchedCharacters
        else state.searchedCharacters.map { group ->
            group.copy(
                characters = group.characters.filter { character ->
                    character.name.contains(searchedName, ignoreCase = true) ||
                    character.aliases.any { it.contains(searchedName, ignoreCase = true) }
                }
            )
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
                        Text(text = "Поиск по названию")
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { searchedName = "" }
                        ) {
                            Icon(
                                painter = painterResource(Res.image.ic_cancel),
                                contentDescription = "Очистить поиск",
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
                    filteredList.forEach { group ->
                        item {
                            Text(
                                text = group.fandomName,
                                modifier = Modifier.padding(6.dp),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
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
                        Text("Отмена")
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchedFandomItem(
    fandom: SearchedFandomModel,
    onSelected: (SearchedFandomModel) -> Unit
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
    onSelected: (SearchedTagModel) -> Unit
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
                    painter = painterResource(Res.image.ic_18),
                    contentDescription = "Иконка 18+",
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
    onClick: () -> Unit
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
        if(character.aliases.isNotEmpty()) {
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
    onModifierChange: (modifier: String) -> Unit
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
                    if(character.modifier.isNotEmpty()) {
                        Text(
                            text = character.modifier,
                            style = MaterialTheme.typography.labelLarge
                        )
                    } else {
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        painter = painterResource(Res.image.ic_arrow_down),
                        contentDescription = "Иконка стрелка вниз",
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
                                    Text("Добавить свой")
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            onModifierChange(textFieldValue)
                                            expanded = false
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.image.ic_check),
                                            contentDescription = "Иконка галочка",
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
                if(expanded && subtitle != null) {
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
                    painter = painterResource(Res.image.ic_cancel),
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
    onClick: (Boolean) -> Unit
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
    onClick: (Boolean) -> Unit
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
private fun <T : Any> ExposedMenuSelector(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    selectedItemName: String,
    items: List<Pair<String, T>>,
    onItemClicked: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier
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
                        onItemClicked(pair.second)
                        onExpandedChange(false)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun VerticalCategorySpacer() = Spacer(modifier = Modifier.height(spaceBetweenCategories))

private val spaceBetweenCategories = 8.dp
private val spaceBetweenItems = 6.dp