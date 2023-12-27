package ru.blays.ficbookReader.components.searchContent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.example.myapplication.compose.Res
import com.moriatsushi.insetsx.systemBarsPadding
import io.github.skeptick.libres.compose.painterResource
import kotlinx.coroutines.launch
import ru.blays.ficbookReader.components.fanficsList.FanficsListContent
import ru.blays.ficbookReader.shared.data.dto.IntRangeSimple
import ru.blays.ficbookReader.shared.data.dto.SearchParams
import ru.blays.ficbookReader.shared.data.dto.SearchedFandomModel
import ru.blays.ficbookReader.shared.data.dto.SearchedTagModel
import ru.blays.ficbookReader.shared.ui.fanficListComponents.FanficsListComponent
import ru.blays.ficbookReader.shared.ui.searchComponents.declaration.SearchComponent
import ru.blays.ficbookReader.shared.ui.searchComponents.declaration.SearchFandomsComponent
import ru.blays.ficbookReader.shared.ui.searchComponents.declaration.SearchTagsComponent
import ru.blays.ficbookReader.ui_components.LazyItems.items
import ru.blays.ficbookReader.utils.surfaceColorAtAlpha
import ru.blays.ficbookReader.values.CardShape
import ru.blays.ficbookReader.values.DefaultPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingsToolbar

@Composable
fun SearchContent(component: SearchComponent) {
    BoxWithConstraints(
        modifier = Modifier.systemBarsPadding(),
    ) {
        if(maxWidth > 600.dp) {
            LandscapeContent(component)
        } else {
            PortraitContent(component)
        }
    }
}

@Composable
private fun LandscapeContent(component: SearchComponent) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Open)
    Scaffold(
        topBar = {
            CollapsingsToolbar(
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
                            scope.launch {
                                if(drawerState.isOpen) {
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
                collapsingTitle = CollapsingTitle.large("Поиск")
            )
        }
    ) { padding ->
        ModalNavigationDrawer(
            gesturesEnabled = false,
            drawerContent = {
                ModalDrawerSheet {
                    SearchMenu(
                        component = component,
                        modifier = Modifier
                            .padding(DefaultPadding.CardDefaultPadding)
                            .width(450.dp),
                    )
                }
            },
            drawerState = drawerState,
            modifier = Modifier.padding(
                top = padding.calculateTopPadding()
            )
        ) {
            FanficsListContent(
                component = component.fanficsListComponent
            )
        }
    }
}

@Composable
private fun PortraitContent(component: SearchComponent) {
    val bottomSheetState = rememberModalBottomSheetState(false)
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(bottomSheetState)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            bottomSheetState.expand()
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
            CollapsingsToolbar(
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
                collapsingTitle = CollapsingTitle.large("Поиск")
            )
        },
        scaffoldState = bottomSheetScaffoldState
    ) { padding ->
        FanficsListContent(
            component = component.fanficsListComponent,
            modifier = Modifier.padding(top = padding.calculateTopPadding()),
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
            CategorySpacer()
            FandomFilter(
                value = state.fandomsFilter,
                onValueChange = component::setFandomsFilter
            )
            CategorySpacer()
            AnimatedVisibility(
                visible = state.fandomsFilter == SearchParams.FANDOM_FILTER_CATEGORY,
                enter = expandVertically(spring()),
                exit = shrinkVertically(spring())
            ) {
                FandomGroupSelector(
                    value = state.fandomsGroup,
                    onValueChange = component::setFandomsGroup
                )
                CategorySpacer()
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
                CategorySpacer()
            }
            TagsSelector(
                component = component.searchTagsComponent,
            )
            CategorySpacer()
            PagesRangeSelector(
                value = state.pagesCountRange,
                onSelect = component::setPagesCountRange
            )
            CategorySpacer()
            StatusSelector(
                value = state.withStatus,
                onSelect = component::setStatus
            )
            CategorySpacer()
            RatingSelector(
                value = state.withRating,
                onSelect = component::setRating
            )
            CategorySpacer()
            DirectionSelector(
                value = state.withDirection,
                onSelect = component::setDirection
            )
            TranslateSelector(
                value = state.translate,
                onSelect = component::setTranslate
            )
            CategorySpacer()
            CheckboxWithTitle(
                checked = state.onlyPremium,
                title = "Фанфики из раздела «Горячие работы»",
                onClick = component::setOnlyPremium
            )
            CategorySpacer()
            LikesRangeSelector(
                value = state.likesRange,
                onSelect = component::setLikesRange
            )
            CategorySpacer()
            RewardsCountSelector(
                value = state.minRewards,
                onSelect = component::setMinRewards
            )
            CategorySpacer()
            CheckboxWithTitle(
                checked = state.filterReaded,
                title = "Не показывать прочитанное",
                onClick = component::setFilterReaded
            )
            CategorySpacer()
            SortTypeSelector(
                value = state.sort,
                onSelect = component::setSort
            )
            CategorySpacer()
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FandomsSelector(
    component: SearchFandomsComponent,
    canIncludeFandoms: Boolean
) {
    val state by component.state.subscribeAsState()

    var chipExpanded by rememberSaveable { mutableStateOf(false) }

    val excludedColor = MaterialTheme.colorScheme.surfaceVariant
    val includedColor = MaterialTheme.colorScheme.surfaceColorAtAlpha(0.45F)
    val contentColor = MaterialTheme.colorScheme.onBackground

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
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(5.dp),
            ) {
                if (canIncludeFandoms) {
                    FlowRow {
                        state.selectedFandoms.forEach { fandom ->
                            ItemChip(
                                title = fandom.title,
                                subtitle = fandom.description,
                                expanded = chipExpanded,
                                excluded = false,
                                includedColor = includedColor,
                                excludedColor = excludedColor,
                                contentColor = contentColor,
                                onRemove = {
                                    component.selectFandom(
                                        select = false,
                                        fandom = fandom
                                    )
                                }
                            )
                        }
                    }
                }
                FlowRow {
                    state.excludedFandoms.forEach { fandom ->
                        ItemChip(
                            title = fandom.title,
                            subtitle = fandom.description,
                            expanded = chipExpanded,
                            excluded = true,
                            includedColor = includedColor,
                            excludedColor = excludedColor,
                            contentColor = contentColor,
                            onRemove = {
                                component.excludeFandom(
                                    exclude = false,
                                    fandom = fandom
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                var selectIncludedFandomDialogVisible by remember { mutableStateOf(false) }
                var selectExcludedFandomDialogVisible by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spaceBetweenItems)
                ) {
                    if (canIncludeFandoms) {
                        OutlinedButton(
                            onClick = {
                                selectIncludedFandomDialogVisible = true
                            },
                            modifier = Modifier.weight(0.5F)
                        ) {
                            Icon(
                                painter = painterResource(Res.image.ic_plus),
                                contentDescription = "Исконка плюс",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.requiredWidth(6.dp))
                            Text("Добавить")
                        }
                        Spacer(Modifier.requiredWidth(6.dp))
                    }
                    OutlinedButton(
                        onClick = {
                            selectExcludedFandomDialogVisible = true
                        },
                        modifier = Modifier.weight(0.5F)
                    ) {
                        Icon(
                            painter = painterResource(Res.image.ic_cross),
                            contentDescription = "Иконка крест",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.requiredWidth(6.dp))
                        Text("Исключить")
                    }
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
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsSelector(
    component: SearchTagsComponent
) {
    val state by component.state.subscribeAsState()

    var chipExpanded by rememberSaveable { mutableStateOf(false) }

    val excludedColor = MaterialTheme.colorScheme.surfaceVariant
    val includedColor = MaterialTheme.colorScheme.surfaceColorAtAlpha(0.45F)
    val contentColor = MaterialTheme.colorScheme.onBackground
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
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(5.dp),
            ) {
                FlowRow {
                    state.selectedTags.forEach { tag ->
                        ItemChip(
                            title = tag.title,
                            subtitle = tag.description,
                            expanded = chipExpanded,
                            excluded = false,
                            includedColor = includedColor,
                            excludedColor = excludedColor,
                            contentColor = contentColor,
                            onRemove = {
                                component.selectTag(
                                    select = false,
                                    tag = tag
                                )
                            }
                        )
                    }
                }
                FlowRow {
                    state.excludedTags.forEach { tag ->
                        ItemChip(
                            title = tag.title,
                            subtitle = tag.description,
                            expanded = chipExpanded,
                            excluded = true,
                            includedColor = includedColor,
                            excludedColor = excludedColor,
                            contentColor = contentColor,
                            onRemove = {
                                component.excludeTag(
                                    exclude = false,
                                    tag = tag
                                )
                            },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                var selectIncludedTagDialogVisible by remember { mutableStateOf(false) }
                var selectExcludedTagDialogVisible by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spaceBetweenItems)
                ) {
                    OutlinedButton(
                        onClick = {
                            selectIncludedTagDialogVisible = true
                        },
                        modifier = Modifier.weight(0.5F)
                    ) {
                        Icon(
                            painter = painterResource(Res.image.ic_plus),
                            contentDescription = "Исконка плюс",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.requiredWidth(6.dp))
                        Text("Добавить")
                    }
                    Spacer(Modifier.requiredWidth(6.dp))
                    OutlinedButton(
                        onClick = {
                            selectExcludedTagDialogVisible = true
                        },
                        modifier = Modifier.weight(0.5F)
                    ) {
                        Icon(
                            painter = painterResource(Res.image.ic_cross),
                            contentDescription = "Иконка крест",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.requiredWidth(6.dp))
                        Text("Исключить")
                    }
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
        }
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
                    if(it) {
                        value + SearchParams.STATUS_IN_PROGRESS
                    }
                    else {
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
                    if(it) {
                        value + SearchParams.STATUS_COMPLETED
                    }
                    else {
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
                    if(it) {
                        value + SearchParams.STATUS_FROZEN
                    }
                    else {
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
                    if(it) {
                        value + SearchParams.RATING_G
                    }
                    else {
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
                    if(it) {
                        value + SearchParams.RATING_PG13
                    }
                    else {
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
                    if(it) {
                        value + SearchParams.RATING_R
                    }
                    else {
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
                    if(it) {
                        value + SearchParams.RATING_NC17
                    }
                    else {
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
                    if(it) {
                        value + SearchParams.RATING_NC21
                    }
                    else {
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
                    if(it) {
                        value + SearchParams.DIRECTION_GEN
                    }
                    else {
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
                    if(it) {
                        value + SearchParams.DIRECTION_HET
                    }
                    else {
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
                    if(it) {
                        value + SearchParams.DIRECTION_SLASH
                    }
                    else {
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
                    if(it) {
                        value + SearchParams.DIRECTION_FEMSLASH
                    }
                    else {
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
                    if(it) {
                        value + SearchParams.DIRECTION_OTHER
                    }
                    else {
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
                    if(it) {
                        value + SearchParams.DIRECTION_MIXED
                    }
                    else {
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
            value = if(value == 0) "" else "$value",
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
            value = if(value.start == 0) "" else "${value.start}",
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
            value = if(value.end == 0) "" else "${value.end}",
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
                CategorySpacer()
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
                CategorySpacer()
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
            if(tag.isAdult) {
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
private fun ItemChip(
    title: String,
    subtitle: String,
    excluded: Boolean,
    includedColor: Color,
    excludedColor: Color,
    contentColor: Color,
    onRemove: () -> Unit,
    expanded: Boolean
) {
    InputChip(
        selected = !excluded,
        onClick = onRemove,
        label = {
            Column {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (expanded) {
                    Text(
                        text = subtitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        trailingIcon = {
            Icon(
                painter = painterResource(Res.image.ic_cancel),
                contentDescription = null,
                modifier = Modifier.size(21.dp).padding(2.dp)
            )
        },
        colors = InputChipDefaults.inputChipColors(
            containerColor = excludedColor,
            selectedContainerColor = includedColor,
            labelColor = contentColor,
            selectedLabelColor = contentColor,
            trailingIconColor = contentColor,
            selectedTrailingIconColor = contentColor
        ),
        modifier = Modifier.padding(2.dp)
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
private fun <T: Any> ExposedMenuSelector(
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
private fun CategorySpacer() =
    Spacer(modifier = Modifier.height(spaceBetweenCategories))

private val spaceBetweenCategories = 8.dp
private val spaceBetweenItems = 6.dp