package ru.blays.ficbook.components.collectionContent

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import ficbook_reader.compose_ui.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.components.fanficsList.FanficsListContent
import ru.blays.ficbook.components.searchContent.DialogPlatform
import ru.blays.ficbook.components.settings.SettingsSwitchWithTitle
import ru.blays.ficbook.reader.shared.components.collectionComponents.declaration.CollectionPageComponent
import ru.blays.ficbook.reader.shared.components.collectionComponents.implementation.EditCollectionComponent
import ru.blays.ficbook.reader.shared.components.fanficListComponents.declaration.FanficsListComponent
import ru.blays.ficbook.reader.shared.data.dto.CollectionPageModelStable
import ru.blays.ficbook.ui_components.CustomBottomSheetScaffold.EnhancedBottomSheetScaffold
import ru.blays.ficbook.ui_components.CustomBottomSheetScaffold.SheetValue
import ru.blays.ficbook.ui_components.CustomBottomSheetScaffold.rememberBottomSheetScaffoldState
import ru.blays.ficbook.ui_components.CustomBottomSheetScaffold.rememberSheetState
import ru.blays.ficbook.ui_components.spacers.HorizontalSpacer
import ru.blays.ficbook.ui_components.spacers.VerticalSpacer
import ru.blays.ficbook.utils.LocalGlassEffectConfig
import ru.blays.ficbook.utils.thenIf
import ru.blays.ficbook.values.DefaultPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingToolbar

@Composable
fun CollectionPageContent(component: CollectionPageComponent) {
    BoxWithConstraints {
        if (maxWidth > 600.dp) {
            LandscapeContent(component)
        } else {
            PortraitContent(component)
        }
    }
    val dialogComponent = component.editDialog.subscribeAsState().value.child?.instance
    dialogComponent?.let { EditCollectionDialog(it) }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun LandscapeContent(component: CollectionPageComponent) {
    val state by component.state.subscribeAsState()
    var sortParamsOpened by rememberSaveable { mutableStateOf(false) }

    val glassEffectConfig = LocalGlassEffectConfig.current
    val hazeState = remember { HazeState() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .thenIf(glassEffectConfig.blurEnabled) {
                        hazeChild(
                            state = hazeState,
                            style = glassEffectConfig.style
                        )
                    }
                    .background(
                        color = if (glassEffectConfig.blurEnabled) {
                            Color.Transparent
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    )
            ) {
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
                    collapsingTitle = CollapsingTitle.small(state.collectionPage?.name ?: ""),
                    containerColor = Color.Transparent,
                    collapsedElevation = if (glassEffectConfig.blurEnabled) 0.dp else 4.dp,
                    insets = WindowInsets.statusBars,
                )
                state.collectionPage?.let { Header(it) }
                when(
                    val page = state.collectionPage
                ) {
                    is CollectionPageModelStable.Other -> OtherCollectionActions(
                        page = page,
                        component = component,
                        sortParamsOpenedChange = { sortParamsOpened = !sortParamsOpened }
                    )
                    is CollectionPageModelStable.Own -> OwnCollectionActions(
                        page = page,
                        component = component,
                        sortParamsOpenedChange = { sortParamsOpened = !sortParamsOpened }
                    )
                    else -> {}
                }
            }
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

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun PortraitContent(component: CollectionPageComponent) {
    val state by component.state.subscribeAsState()
    val bottomSheetState = rememberSheetState(
        skipPartiallyExpanded = false,
        initialValue = SheetValue.PartiallyExpanded
    )
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(bottomSheetState)
    val coroutineScope = rememberCoroutineScope()

    val glassEffectConfig = LocalGlassEffectConfig.current
    val hazeState = remember { HazeState() }

    EnhancedBottomSheetScaffold(
        modifier = Modifier.fillMaxSize(),
        sheetPeekHeight = 0.dp,
        sheetContent = {
            SortParamContent(component = component) {
                coroutineScope.launch {
                    bottomSheetState.partialExpand()
                }
            }
            Spacer(modifier = Modifier.navigationBarsPadding())
        },
        topBar = {
            Column(
                modifier = Modifier
                    .thenIf(glassEffectConfig.blurEnabled) {
                        hazeChild(
                            state = hazeState,
                            style = glassEffectConfig.style
                        )
                    }
                    .background(
                        color = if (glassEffectConfig.blurEnabled) {
                            Color.Transparent
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
            ) {
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
                    collapsingTitle = CollapsingTitle.small(state.collectionPage?.name ?: ""),
                    containerColor = Color.Transparent,
                    collapsedElevation = if (glassEffectConfig.blurEnabled) 0.dp else 4.dp,
                    insets = WindowInsets.statusBars
                )
                state.collectionPage?.let { Header(it) }
                when (
                    val page = state.collectionPage
                ) {
                    is CollectionPageModelStable.Other -> OtherCollectionActions(
                        page = page,
                        component = component,
                        sortParamsOpenedChange = {
                            coroutineScope.launch {
                                if (bottomSheetState.currentValue == SheetValue.Expanded) {
                                    bottomSheetState.partialExpand()
                                } else {
                                    bottomSheetState.expand()
                                }
                            }
                        }
                    )

                    is CollectionPageModelStable.Own -> OwnCollectionActions(
                        page = page,
                        component = component,
                        sortParamsOpenedChange = {
                            coroutineScope.launch {
                                if (bottomSheetState.currentValue == SheetValue.Expanded) {
                                    bottomSheetState.partialExpand()
                                } else {
                                    bottomSheetState.expand()
                                }
                            }
                        }
                    )

                    else -> {}
                }
                VerticalSpacer(3.dp)
            }
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

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun SortParamContent(
    modifier: Modifier = Modifier,
    component: CollectionPageComponent,
    onDismissRequest: () -> Unit = {},
) {
    val state by component.state.subscribeAsState()

    var fandomMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var directionMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var sortMenuExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(DefaultPadding.CardDefaultPadding),
    ) {
        OutlinedTextField(
            value = state.currentParams.searchText ?: "",
            onValueChange = {
                component.sendIntent(
                    CollectionPageComponent.Intent.ChangeSearchText(it)
                )
            },
            label = {
                Text(text = stringResource(Res.string.collection_search_by_name))
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        component.sendIntent(
                            CollectionPageComponent.Intent.ChangeSearchText("")
                        )
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
        Spacer(modifier = Modifier.requiredHeight(10.dp))
        state.collectionPage?.filterParams?.availableFandoms?.let { items ->
            DropDownSelector(
                expanded = fandomMenuExpanded,
                onExpandedChange = {
                    fandomMenuExpanded = it
                },
                selectedItemName = state.currentParams.fandom?.first
                    ?: stringResource(Res.string.collection_search_every_fandom),
                items = items,
                onItemClicked = { item ->
                    component.sendIntent(
                        CollectionPageComponent.Intent.ChangeFandom(item)
                    )
                }
            )
        }
        Spacer(modifier = Modifier.requiredHeight(10.dp))
        state.collectionPage?.filterParams?.availableDirections?.let { items ->
            DropDownSelector(
                expanded = directionMenuExpanded,
                onExpandedChange = {
                    directionMenuExpanded = it
                },
                selectedItemName = state.currentParams.direction?.first
                    ?: stringResource(Res.string.collection_search_every_direction),
                items = items,
                onItemClicked = { item ->
                    component.sendIntent(
                        CollectionPageComponent.Intent.ChangeDirection(item)
                    )
                }
            )
        }
        Spacer(modifier = Modifier.requiredHeight(10.dp))
        state.collectionPage?.filterParams?.availableSortParams?.let { items ->
            DropDownSelector(
                expanded = sortMenuExpanded,
                onExpandedChange = {
                    sortMenuExpanded = it
                },
                selectedItemName = state.currentParams.sort?.first
                    ?: stringResource(Res.string.collection_search_sort_default),
                items = items,
                onItemClicked = { item ->
                    component.sendIntent(
                        CollectionPageComponent.Intent.ChangeSortType(item)
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
                    component.sendIntent(
                        CollectionPageComponent.Intent.ClearFilter
                    )
                    onDismissRequest()
                }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_cross),
                    contentDescription = stringResource(Res.string.content_description_icon_clear),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.requiredWidth(8.dp))
                Text(
                    text = stringResource(Res.string.reset)
                )
            }
            Spacer(modifier = Modifier.requiredWidth(14.dp))
            Button(
                onClick = {
                    component.sendIntent(
                        CollectionPageComponent.Intent.Search
                    )
                    onDismissRequest()
                }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_search),
                    contentDescription = stringResource(Res.string.content_description_icon_search),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.requiredWidth(8.dp))
                Text(text = stringResource(Res.string.action_search))
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

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun EditCollectionDialog(component: EditCollectionComponent) {
    val state by component.state.subscribeAsState()
    DialogPlatform(
        dismissOnClickOutside = false,
        onDismissRequest = component::cancel,
    ) {
        Card {
            Column(
                modifier = Modifier.padding(DefaultPadding.CardDefaultPadding),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(Res.string.collection_edit),
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                AnimatedVisibility(
                    visible = state.error
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = CardDefaults.shape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.errorMessage ?: stringResource(Res.string.error_something_went_wrong),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
                OutlinedTextField(
                    value = state.name,
                    onValueChange = component::onNameChange,
                    label = { Text(text = stringResource(Res.string.title)) },
                    singleLine = true,
                    shape = CardDefaults.shape,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.descriptor,
                    onValueChange = component::onDescriptorChange,
                    label = { Text(text = stringResource(Res.string.description)) },
                    maxLines = 5,
                    shape = CardDefaults.shape,
                    modifier = Modifier.fillMaxWidth(),
                )
                SettingsSwitchWithTitle(
                    title = stringResource(Res.string.collection_visibility_type),
                    state = state.public,
                    enabled = true,
                    action = component::onPublicChange
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = component::cancel) {
                        Text(text = stringResource(Res.string.cancel))
                    }
                    HorizontalSpacer(8.dp)
                    Button(onClick = component::confirm) {
                        Text(text = stringResource(Res.string.save))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun Header(
    page: CollectionPageModelStable
) {
    page.description?.let { description ->
        Column(
            modifier = Modifier.padding(
                horizontal = DefaultPadding.CardHorizontalPadding
            ),
        ) {
            Text(
                text = stringResource(Res.string.description) + ":",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 5.dp),
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun OwnCollectionActions(
    page: CollectionPageModelStable.Own,
    component: CollectionPageComponent,
    sortParamsOpenedChange: () -> Unit
) {
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val containerColor = MaterialTheme.colorScheme.primary.copy(
            alpha = 0.25f
        )
        val colors = ButtonDefaults.textButtonColors(
            containerColor = containerColor
        )

        TextButton(
            onClick = {
                component.sendIntent(
                    CollectionPageComponent.Intent.Edit
                )
            },
            colors = colors
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_edit),
                contentDescription = stringResource(Res.string.content_description_icon_edit),
                modifier = Modifier.size(16.dp),
            )
            HorizontalSpacer(6.dp)
            Text(
                text = stringResource(Res.string.action_change)
            )
        }

        TextButton(
            onClick = { showConfirmDeleteDialog = true },
            colors = colors
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_delete),
                contentDescription = stringResource(Res.string.content_description_icon_delete),
                modifier = Modifier.size(16.dp),
            )
            HorizontalSpacer(6.dp)
            Text(
                text = stringResource(Res.string.delete)
            )
        }
        Spacer(modifier = Modifier.weight(1F))
        TextButton(
            onClick = sortParamsOpenedChange,
            colors = colors
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_sort),
                contentDescription = stringResource(Res.string.content_description_icon_sort),
                modifier = Modifier.size(16.dp),
            )
            HorizontalSpacer(6.dp)
            Text(
                text = stringResource(Res.string.sort)
            )
        }
    }
    if(showConfirmDeleteDialog) {
        DeleteCollectionConfirmDialog(
            onConfirm = {
                component.sendIntent(
                    CollectionPageComponent.Intent.Delete
                )
                showConfirmDeleteDialog = false
            },
            onDismiss = { showConfirmDeleteDialog = false }
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun OtherCollectionActions(
    page: CollectionPageModelStable.Other,
    component: CollectionPageComponent,
    sortParamsOpenedChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val containerColor = MaterialTheme.colorScheme.primary.copy(
            alpha = 0.25f
        )
        val colors = ButtonDefaults.textButtonColors(
            containerColor = containerColor
        )

        val starIcon = if(page.subscribed) {
            painterResource(Res.drawable.ic_star_filled)
        } else {
            painterResource(Res.drawable.ic_star_outlined)
        }

        TextButton(
            onClick = {
                component.sendIntent(
                    CollectionPageComponent.Intent.ChangeSubscription(!page.subscribed)
                )
            },
            colors = colors
        ) {
            Icon(
                painter = starIcon,
                contentDescription = stringResource(Res.string.content_description_icon_sort),
                modifier = Modifier.size(16.dp),
            )
            HorizontalSpacer(6.dp)
            Text(
                text = if(page.subscribed) {
                    stringResource(Res.string.action_unfollow)
                } else {
                    stringResource(Res.string.action_follow)
                }
            )
        }

        Spacer(modifier = Modifier.weight(1F))
        TextButton(
            onClick = sortParamsOpenedChange,
            colors = colors
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_sort),
                contentDescription = stringResource(Res.string.content_description_icon_sort),
                modifier = Modifier.size(16.dp),
            )
            HorizontalSpacer(6.dp)
            Text(
                text = stringResource(Res.string.sort)
            )
        }
    }
}