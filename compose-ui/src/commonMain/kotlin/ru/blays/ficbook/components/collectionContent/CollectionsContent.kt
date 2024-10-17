package ru.blays.ficbook.components.collectionContent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import ficbook_reader.compose_ui.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.components.settingsContent.SettingsSwitchWithTitle
import ru.blays.ficbook.reader.shared.components.collectionComponents.declaration.CollectionsListComponent
import ru.blays.ficbook.reader.shared.data.dto.CollectionCardModelStable
import ru.blays.ficbook.ui_components.ContextMenu.ContextMenu
import ru.blays.ficbook.ui_components.ContextMenu.contextMenuAnchor
import ru.blays.ficbook.ui_components.ContextMenu.rememberContextMenuState
import ru.blays.ficbook.ui_components.dialogComponents.DialogPlatform
import ru.blays.ficbook.ui_components.spacers.HorizontalSpacer
import ru.blays.ficbook.ui_components.spacers.VerticalSpacer
import ru.blays.ficbook.utils.LocalBlurState
import ru.blays.ficbook.utils.primaryColorAtAlpha
import ru.blays.ficbook.utils.thenIf
import ru.blays.ficbook.values.CardShape
import ru.blays.ficbook.values.DefaultPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingToolbar

@Composable
fun CollectionsContentExtended(
    component: CollectionsListComponent,
    contentPadding: PaddingValues?
) {
    Column {
        VerticalSpacer(contentPadding?.calculateTopPadding() ?: 0.dp)
        CollectionsControlContent(component)
        CollectionsContent(
            component = component,
            contentPadding = null
        )
    }
}

@Composable
fun CollectionsContent(
    modifier: Modifier = Modifier,
    component: CollectionsListComponent,
    contentPadding: PaddingValues?
) {
    val state by component.state.subscribeAsState()
    val list = remember(state) { state.list }
    val isLoading = remember(state) { state.isLoading }

    val pullRefreshState = rememberPullToRefreshState()

    BoxWithConstraints {
        val columnsCount = when(maxWidth) {
            in 800.dp..Dp.Infinity -> 3
            in 500.dp..800.dp -> 2
            else -> 1
        }
        PullToRefreshBox(
            state = pullRefreshState,
            isRefreshing = isLoading,
            onRefresh = {
                component.sendIntent(
                    CollectionsListComponent.Intent.Refresh
                )
            }
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columnsCount),
                contentPadding = contentPadding ?: PaddingValues(0.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = modifier
                    .padding(DefaultPadding.CardDefaultPadding)
                    .fillMaxSize(),
            ) {
                items(list) { collection ->
                    CollectionItem(
                        collectionModel = collection,
                        component = component
                    )
                }
            }
        }
    }
}

@Composable
fun CollectionsScreenContent(component: CollectionsListComponent) {
    val blurEnabled = LocalBlurState.current
    val hazeState = remember(::HazeState)

    Scaffold(
        topBar = {
            CollapsingToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.onOutput(
                                CollectionsListComponent.Output.NavigateBack
                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.content_description_icon_back)
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.small(stringResource(Res.string.author_profile_tab_collections)),
                containerColor = if(blurEnabled) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.surface
                },
                collapsedElevation = if(blurEnabled) 0.dp else 4.dp,
                insets = WindowInsets.statusBars,
                modifier = Modifier.thenIf(blurEnabled) {
                    hazeChild(state = hazeState)
                },
            )
        }
    ) { padding ->
        CollectionsContent(
            component = component,
            contentPadding = padding,
            modifier = Modifier.thenIf(blurEnabled) {
                haze(hazeState)
            },
        )
    }
}


@Composable
private fun CollectionItem(
    collectionModel: CollectionCardModelStable,
    component: CollectionsListComponent,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardDefaults.shape),
    ) {
        when(collectionModel) {
            is CollectionCardModelStable.Other -> OtherCollectionContent(
                collection = collectionModel,
                onClick = {
                    component.onOutput(
                        CollectionsListComponent.Output.OpenCollection(
                            collectionModel
                        )
                    )
                },
                onUserClick = {
                    component.onOutput(
                        CollectionsListComponent.Output.OpenUser(collectionModel.owner)
                    )
                },
                onChangeSubscription = {
                    component.sendIntent(
                        CollectionsListComponent.Intent.ChangeSubscription(
                            collection = collectionModel
                        )
                    )
                }
            )
            is CollectionCardModelStable.Own -> OwnCollectionContent(
                component = component,
                collection = collectionModel,
            )
        }
    }
}

@Composable
private fun OwnCollectionContent(
    modifier: Modifier = Modifier,
    component: CollectionsListComponent,
    collection: CollectionCardModelStable.Own
) {
    val contextMenuState = rememberContextMenuState()

    val indicatorIcon = if (collection.public) {
        painterResource(Res.drawable.ic_unlock)
    } else {
        painterResource(Res.drawable.ic_lock)
    }

    ConstraintLayout(
        modifier = modifier
            .combinedClickable(
                onClick = {
                    component.onOutput(
                        CollectionsListComponent.Output.OpenCollection(
                            collection = collection
                        )
                    )
                },
                onLongClick = contextMenuState::show
            )
            .heightIn(min = minItemContentHeight)
            .fillMaxWidth()
            .contextMenuAnchor(contextMenuState)
    ) {
        val (icon, title, size) = createRefs()

        Icon(
            painter = indicatorIcon,
            contentDescription = stringResource(Res.string.content_description_icon_lock),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(30.dp)
                .constrainAs(icon) {
                    start.linkTo(parent.start, margin = 8.dp)
                    centerVerticallyTo(parent)
                }
        )
        Text(
            text = collection.name,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.constrainAs(title) {
                start.linkTo(icon.end, margin = 8.dp)
                centerVerticallyTo(icon)
            },
        )
        Box(
            modifier = Modifier
                .height(36.dp)
                .widthIn(min = 36.dp, max = 50.dp)
                .background(
                    MaterialTheme.colorScheme.background,
                    CardShape.CardStandalone,
                )
                .constrainAs(size) {
                    end.linkTo(parent.end, margin = 8.dp)
                    centerVerticallyTo(parent)
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${collection.size}",
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(2.dp),
            )
        }
    }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    ContextMenu(
        state = contextMenuState,
    ) {
        DropdownMenuItem(
            text = {
                Text(text = stringResource(Res.string.action_change))
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_edit),
                    contentDescription = stringResource(Res.string.content_description_icon_edit),
                    modifier = Modifier.size(20.dp)
                )
            },
            onClick = {
                component.sendIntent(
                    CollectionsListComponent.Intent.UpdateCollection(
                        collection = collection
                    )
                )
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(Res.string.delete)) },
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_delete),
                    contentDescription = stringResource(Res.string.content_description_icon_delete),
                    modifier = Modifier.size(20.dp)
                )
            },
            onClick = {
                showDeleteConfirmDialog = true
                contextMenuState.hide()
            }
        )
    }
    if (showDeleteConfirmDialog) {
        DeleteCollectionConfirmDialog(
            onConfirm = {
                component.sendIntent(
                    CollectionsListComponent.Intent.DeleteCollection(collection.realID)
                )
                showDeleteConfirmDialog = false
            },
            onDismiss = {
                showDeleteConfirmDialog = false
            }
        )
    }
}

@Composable
private fun OtherCollectionContent(
    modifier: Modifier = Modifier,
    collection: CollectionCardModelStable.Other,
    onClick: () -> Unit,
    onUserClick: () -> Unit,
    onChangeSubscription: () -> Unit
) {
    val contextMenuState = rememberContextMenuState()

    val indicatorIcon = if (collection.subscribed) {
        painterResource(Res.drawable.ic_star_filled)
    } else {
        painterResource(Res.drawable.ic_star_outlined)
    }

    ConstraintLayout(
        modifier = modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = contextMenuState::show
            )
            .heightIn(min = minItemContentHeight)
            .fillMaxWidth()
            .contextMenuAnchor(contextMenuState),
    ) {
        val (
            icon,
            title,
            ownerIcon,
            ownerName,
            size
        ) = createRefs()

        Icon(
            painter = indicatorIcon,
            contentDescription = stringResource(Res.string.content_description_icon_star),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(30.dp)
                .constrainAs(icon) {
                    start.linkTo(parent.start, margin = 8.dp)
                    centerVerticallyTo(parent)
                }
        )
        Text(
            text = collection.name,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.constrainAs(title) {
                start.linkTo(icon.end, margin = 8.dp)
                end.linkTo(size.start)
                width = Dimension.fillToConstraints
            },
        )
        Icon(
            painter = painterResource(Res.drawable.ic_user),
            contentDescription = stringResource(Res.string.content_description_icon_user),
            modifier = Modifier
                .size(16.dp)
                .constrainAs(ownerIcon) {
                    start.linkTo(title.start)
                    top.linkTo(title.bottom, margin = 4.dp)
                }
        )
        Text(
            text = collection.owner.name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .constrainAs(ownerName) {
                    start.linkTo(ownerIcon.end, margin = 4.dp)
                    centerVerticallyTo(ownerIcon)
                }
                .clickable(onClick = onUserClick)
        )
        Box(
            modifier = Modifier
                .height(36.dp)
                .widthIn(min = 36.dp, max = 50.dp)
                .background(
                    MaterialTheme.colorScheme.background,
                    CardShape.CardStandalone,
                )
                .constrainAs(size) {
                    end.linkTo(parent.end, margin = 8.dp)
                    centerVerticallyTo(parent)
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${collection.size}",
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(2.dp),
            )
        }
    }

    ContextMenu(
        state = contextMenuState,
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    text = if(collection.subscribed) {
                        stringResource(Res.string.collection_action_unfollow)
                    } else {
                        stringResource(Res.string.collection_action_follow)
                    }
                )
            },
            leadingIcon = {
                Icon(
                    painter = if(collection.subscribed) {
                        painterResource(Res.drawable.ic_star_outlined)
                    } else {
                        painterResource(Res.drawable.ic_star_filled)
                    },
                    contentDescription = stringResource(Res.string.content_description_icon_edit),
                    modifier = Modifier.size(20.dp)
                )
            },
            onClick = {
                onChangeSubscription()
                contextMenuState.hide()
            }
        )
    }
}

@Composable
private fun CollectionsControlContent(component: CollectionsListComponent) {
    var showCreateCollectionDialog by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.padding(DefaultPadding.CardDefaultPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = { showCreateCollectionDialog = true },
            colors = ButtonDefaults.textButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryColorAtAlpha(0.25F)
            )
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_plus),
                contentDescription = stringResource(Res.string.content_description_icon_plus),
                modifier = Modifier.size(22.dp),
            )
            HorizontalSpacer(4.dp)
            Text(
                text = stringResource(Res.string.collection_action_create),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
    if (showCreateCollectionDialog) {
        CreateCollectionDialog(
            component = component,
            onDismiss = { showCreateCollectionDialog = false }
        )
    }
}

@Composable
fun DeleteCollectionConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) = Dialog(onDismissRequest = onDismiss) {
    Card {
        Column(
            modifier = Modifier.padding(DefaultPadding.CardDefaultPadding),
        ) {
            Text(
                text = stringResource(Res.string.collection_delete_confirm),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onDismiss) {
                    Text(text = stringResource(Res.string.cancel))
                }
                HorizontalSpacer(8.dp)
                Button(onClick = onConfirm) {
                    Text(text = stringResource(Res.string.delete))
                }
            }
        }
    }
}

@Composable
private fun CreateCollectionDialog(
    component: CollectionsListComponent,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var public by remember { mutableStateOf(false) }
    DialogPlatform(
        dismissOnClickOutside = false,
        onDismissRequest = onDismiss,
    ) {
        Card {
            Column(
                modifier = Modifier.padding(DefaultPadding.CardDefaultPadding),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(Res.string.collection_create),
                    style = MaterialTheme.typography.titleLarge,
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
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(text = stringResource(Res.string.description)) },
                    maxLines = 5,
                    shape = CardDefaults.shape,
                    modifier = Modifier.fillMaxWidth(),
                )
                SettingsSwitchWithTitle(
                    title = stringResource(Res.string.collection_visibility_type),
                    state = public,
                    enabled = true,
                    action = { public = it }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text(text = stringResource(Res.string.cancel))
                    }
                    HorizontalSpacer(8.dp)
                    Button(
                        onClick = {
                            component.sendIntent(
                                CollectionsListComponent.Intent.CreateCollection(
                                    name = name,
                                    description = description,
                                    public = public
                                )
                            )
                            onDismiss()
                        }
                    ) {
                        Text(text = stringResource(Res.string.action_create))
                    }
                }
            }
        }
    }
}

private val minItemContentHeight = 55.dp