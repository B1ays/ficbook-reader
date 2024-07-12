package ru.blays.ficbook.components.notifications

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import ficbook_reader.compose_ui.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.reader.shared.components.notificationComponents.NotificationComponent
import ru.blays.ficbook.reader.shared.components.notificationComponents.NotificationConfirmDialogComponent
import ru.blays.ficbook.reader.shared.data.dto.NotificationModelStable
import ru.blays.ficbook.reader.shared.data.dto.NotificationType
import ru.blays.ficbook.utils.LocalGlassEffectConfig
import ru.blays.ficbook.utils.surfaceColorAtAlpha
import ru.blays.ficbook.utils.thenIf
import ru.blays.ficbook.values.CardShape
import ru.blays.ficbook.values.DefaultPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingToolbar

@Composable
fun NotificationsContent(component: NotificationComponent) {
    val lazyListState = rememberLazyListState()

    val canScrollBackward = lazyListState.canScrollBackward
    val canScrollForward = lazyListState.canScrollForward

    val slot by component.slot.subscribeAsState()
    val slotInstance = slot.child?.instance
    slotInstance?.let { slotComponent ->
        ConfirmDialogContent(slotComponent)
    }

    BoxWithConstraints {
        if(maxWidth > 700.dp) {
            LandscapeContent(
                component = component,
                lazyListState = lazyListState
            )
        } else {
            PortraitContent(
                component = component,
                lazyListState = lazyListState
            )
        }
    }

    LaunchedEffect(canScrollForward) {
        if(!canScrollForward && canScrollBackward) {
            component.sendIntent(
                NotificationComponent.Intent.LoadNextPage
            )
        }
    }
}

@Composable
private fun LandscapeContent(
    component: NotificationComponent,
    lazyListState: LazyListState
) {
    val state by component.state.subscribeAsState()

    Scaffold(
        topBar = {
            CollapsingToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.onOutput(
                                NotificationComponent.Output.NavigateBack
                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.content_description_icon_back)
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.large(stringResource(Res.string.notifications)),
                insets = WindowInsets.statusBars
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier.padding(top = padding.calculateTopPadding()),
        ) {
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = DrawerDefaults.shape
                    ),
            ) {
                LazyColumn(
                    modifier = Modifier.padding(6.dp)
                ) {
                    items(state.availableCategories) { category ->
                        DropdownMenuItem(
                            text = { Text(getCategoryName(category.type)) },
                            trailingIcon = {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.surfaceColorAtAlpha(0.65f),
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ) {
                                    Text("${category.notificationsCount}")
                                }
                            },
                            onClick = {
                                component.sendIntent(
                                    NotificationComponent.Intent.SelectCategory(category.type)
                                )
                            }
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Actions(
                    onReadAll = {
                        component.sendIntent(
                            NotificationComponent.Intent.ReadAll
                        )
                    },
                    onDeleteAll = {
                        component.sendIntent(
                            NotificationComponent.Intent.DeleteAll
                        )
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                LazyColumn(
                    state = lazyListState
                ) {
                    items(state.list) { notification ->
                        NotificationItem(
                            notification = notification
                        ) {
                            component.onOutput(
                                NotificationComponent.Output.OpenNotificationHref(notification.href)
                            )
                        }
                        Spacer(modifier = Modifier.requiredHeight(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PortraitContent(
    component: NotificationComponent,
    lazyListState: LazyListState
) {
    val state by component.state.subscribeAsState()

    val glassEffectConfig = LocalGlassEffectConfig.current
    val hazeState = remember { HazeState() }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier.thenIf(glassEffectConfig.blurEnabled) {
                    hazeChild(
                        state = hazeState,
                        style = glassEffectConfig.style
                    )
                }
            ) {
                CollapsingToolbar(
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                component.onOutput(
                                    NotificationComponent.Output.NavigateBack
                                )
                            }
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_arrow_back),
                                contentDescription = stringResource(Res.string.content_description_icon_back)
                            )
                        }
                    },
                    centralContent = {
                        var menuOpened by rememberSaveable { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = menuOpened,
                            onExpandedChange = { menuOpened = it }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(
                                        type = MenuAnchorType.PrimaryNotEditable
                                    ),
                            ) {
                                Text(
                                    text = getCategoryName(state.selectedCategory),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(0.8F, false).animateContentSize(spring()),
                                )
                                Spacer(modifier = Modifier.requiredWidth(6.dp))
                                Icon(
                                    painter = painterResource(Res.drawable.ic_arrow_down),
                                    contentDescription = stringResource(Res.string.content_description_icon_down),
                                    modifier = Modifier.size(20.dp).weight(0.2F, false),
                                )
                            }
                            ExposedDropdownMenu(
                                expanded = menuOpened,
                                onDismissRequest = { menuOpened = false }
                            ) {
                                state.availableCategories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(getCategoryName(category.type)) },
                                        trailingIcon = {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.surfaceColorAtAlpha(0.65f),
                                                contentColor = MaterialTheme.colorScheme.onSurface
                                            ) {
                                                Text("${category.notificationsCount}")
                                            }
                                        },
                                        onClick = {
                                            component.sendIntent(
                                                NotificationComponent.Intent.SelectCategory(category.type)
                                            )
                                            menuOpened = false
                                        }
                                    )
                                }
                            }
                        }
                    },
                    collapsingTitle = null,
                    insets = WindowInsets.statusBars,
                    containerColor = if(glassEffectConfig.blurEnabled) {
                        Color.Transparent
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    collapsedElevation = if(glassEffectConfig.blurEnabled) 0.dp else 4.dp,
                )
                Actions(
                    onReadAll = {
                        component.sendIntent(
                            NotificationComponent.Intent.ReadAll
                        )
                    },
                    onDeleteAll = {
                        component.sendIntent(
                            NotificationComponent.Intent.DeleteAll
                        )
                    },
                    modifier = Modifier
                        .background(
                            color = if(glassEffectConfig.blurEnabled) {
                                Color.Transparent
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                        .padding(DefaultPadding.CardDefaultPadding),
                )
                HorizontalDivider(
                    modifier = Modifier
                        .background(
                            color = if(glassEffectConfig.blurEnabled) {
                                Color.Transparent
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                        .padding(vertical = 6.dp)
                )
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) { padding ->
        LazyColumn(
            state = lazyListState,
            contentPadding = padding,
            modifier = Modifier.thenIf(glassEffectConfig.blurEnabled) {
                haze(state = hazeState,)
            }
        ) {
            items(state.list) { notification ->
                NotificationItem(
                    notification = notification,
                    modifier = Modifier.padding(DefaultPadding.CardDefaultPadding),
                ) {
                    component.onOutput(
                        NotificationComponent.Output.OpenNotificationHref(notification.href)
                    )
                }
            }
        }
    }
}

@Composable
private fun Actions(
    modifier: Modifier = Modifier,
    onReadAll: () -> Unit,
    onDeleteAll: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        OutlinedButton(
            onClick = onReadAll,
            shape = CardShape.CardStandalone,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(
                    alpha = 0.4F
                )
            )
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_check_variant),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.requiredWidth(6.dp))
            Text(text = stringResource(Res.string.notifications_action_read_all))
        }
        Spacer(modifier = Modifier.requiredWidth(10.dp))
        OutlinedButton(
            onClick = onDeleteAll,
            shape = CardShape.CardStandalone,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(
                    alpha = 0.4F
                )
            )
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_delete),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.requiredWidth(6.dp))
            Text(text = stringResource(Res.string.notifications_action_delete_all))
        }
    }
}

@Composable
fun NotificationItem(
    modifier: Modifier = Modifier,
    notification: NotificationModelStable,
    onClick: () -> Unit
) {
    val shape = CardDefaults.shape
    val foregroundCardColor = MaterialTheme.colorScheme.surfaceColorAtAlpha(0.6F)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = foregroundCardColor,
                shape = shape
            )
            .clip(shape)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.padding(DefaultPadding.CardDefaultPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val iconColor = if(notification.readed) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.primary
            }
            Icon(
                painter = getCategoryIcon(notification.type),
                contentDescription = stringResource(Res.string.content_description_icon_category),
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.requiredWidth(10.dp))
            Column(
                modifier = Modifier
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.requiredHeight(4.dp))
                Text(
                    text = notification.date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        val shape2 = RoundedCornerShape(
            topStart = 12.dp,
            topEnd = 12.dp
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = shape2
                )
                .clip(shape2),
        ) {
            Text(
                text = notification.text,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(DefaultPadding.CardDefaultPadding)
            )
        }
    }
}

@Composable
fun ConfirmDialogContent(component: NotificationConfirmDialogComponent) {
    AlertDialog(
        onDismissRequest = {
            component.onOutput(
                NotificationConfirmDialogComponent.Output.Cancel
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    component.onOutput(
                        NotificationConfirmDialogComponent.Output.Confirm
                    )
                }
            ) {
                Text(text = stringResource(Res.string.yes))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    component.onOutput(
                        NotificationConfirmDialogComponent.Output.Cancel
                    )
                }
            ) {
                Text(text = stringResource(Res.string.no))
            }
        },
        title = {
            Text(text = stringResource(Res.string.are_you_sure))
        },
        text = {
            Text(text = stringResource(Res.string.are_you_sure_want, component.actionName))
        }
    )
}

@Composable
private fun getCategoryName(category: NotificationType): String {
    return when(category) {
        NotificationType.ALL_NOTIFICATIONS -> stringResource(Res.string.notifications_category_all)
        NotificationType.DISCUSSION_IN_COMMENTS -> stringResource(Res.string.notifications_category_discussion)
        NotificationType.UPDATES_FROM_SUBSCRIBED_AUTHORS -> stringResource(Res.string.notifications_category_updates_from_subscribed_authors)
        NotificationType.NEW_WORKS_IN_COLLECTIONS -> stringResource(Res.string.notifications_category_new_in_collections)
        NotificationType.UPDATES_IN_FANFICS -> stringResource(Res.string.notifications_category_updates_in_fanfics)
        NotificationType.NEW_BLOGS -> stringResource(Res.string.notifications_category_new_blogs)
        NotificationType.NEW_COMMENTS -> stringResource(Res.string.notifications_category_new_comments)
        NotificationType.SYSTEM_MESSAGES -> stringResource(Res.string.notifications_category_system_messages)
        NotificationType.NEW_WORKS_FOR_LIKED_REQUESTS -> stringResource(Res.string.notifications_category_new_in_liked_requests)
        NotificationType.HELPDESK_RESPONSES -> stringResource(Res.string.notifications_category_helpdesk_responses)
        NotificationType.TEXT_CHANGES_IN_OWN_FANFIC -> ""
        NotificationType.NEW_PRESENTS -> stringResource(Res.string.notifications_category_new_presents)
        NotificationType.NEW_ACHIEVEMENTS -> stringResource(Res.string.notifications_category_new_achievements)
        NotificationType.TEXT_CHANGES_IN_EDITED_FANFIC -> stringResource(Res.string.notifications_category_text_changed_in_edited_fanfic)
        NotificationType.ERROR_MESSAGES -> stringResource(Res.string.notifications_category_messages_about_error)
        NotificationType.REQUESTS_FOR_COAUTHORSHIPS -> stringResource(Res.string.notifications_category_request_for_coauthorship)
        NotificationType.REQUESTS_FOR_BETA_EDITING -> stringResource(Res.string.notifications_category_request_for_beta_editing)
        NotificationType.REQUESTS_FOR_GAMMA_EDITING -> stringResource(Res.string.notifications_category_request_for_gamma_editing)
        NotificationType.NEW_WORKS_ON_MY_REQUESTS -> stringResource(Res.string.notifications_category_new_works_on_my_requests)
        NotificationType.DISCUSSION_IN_REQUEST_COMMENTS -> stringResource(Res.string.notifications_category_discussion_in_request)
        NotificationType.PRIVATE_MESSAGES -> stringResource(Res.string.notifications_category_private_messages)
        NotificationType.NEW_COMMENTS_FOR_REQUEST -> stringResource(Res.string.notifications_category_new_comments_for_request)
        NotificationType.NEW_FANFICS_REWARDS -> stringResource(Res.string.notifications_category_new_fanfic_rewards)
        NotificationType.NEW_COMMENTS_REWARDS -> stringResource(Res.string.notifications_category_new_comments_rewards)
        NotificationType.CHANGES_IN_HEADER_OF_WORK -> stringResource(Res.string.notifications_category_changes_in_work_header)
        NotificationType.COAUTHOR_ADD_NEW_CHAPTER -> stringResource(Res.string.notifications_category_coauthor_add_new_chapter)
        NotificationType.UNKNOWN -> stringResource(Res.string.notifications_category_unknown)
    }
}

@Composable
private fun getCategoryIcon(category: NotificationType): Painter {
    return when(category) {
        NotificationType.ALL_NOTIFICATIONS -> painterResource(Res.drawable.ic_bell)
        NotificationType.DISCUSSION_IN_COMMENTS -> painterResource(Res.drawable.ic_comment)
        NotificationType.UPDATES_FROM_SUBSCRIBED_AUTHORS -> painterResource(Res.drawable.ic_star_outlined)
        NotificationType.NEW_WORKS_IN_COLLECTIONS -> painterResource(Res.drawable.ic_stack_plus)
        NotificationType.UPDATES_IN_FANFICS -> painterResource(Res.drawable.ic_stack_star)
        NotificationType.NEW_BLOGS -> painterResource(Res.drawable.ic_blog)
        else -> painterResource(Res.drawable.ic_dot)
    }
}