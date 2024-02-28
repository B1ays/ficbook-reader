@file:OptIn(ExperimentalResourceApi::class)

package ru.blays.ficbookReader.components.notifications

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
import ficbook_reader.`compose-ui`.generated.resources.*
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import ru.blays.ficbookReader.shared.data.dto.NotificationModelStable
import ru.blays.ficbookReader.shared.data.dto.NotificationType
import ru.blays.ficbookReader.shared.ui.notificationComponents.NotificationComponent
import ru.blays.ficbookReader.shared.ui.notificationComponents.NotificationConfirmDialogComponent
import ru.blays.ficbookReader.utils.LocalGlassEffectConfig
import ru.blays.ficbookReader.utils.surfaceColorAtAlpha
import ru.blays.ficbookReader.utils.thenIf
import ru.blays.ficbookReader.values.CardShape
import ru.blays.ficbookReader.values.DefaultPadding
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
                            contentDescription = "Стрелка назад"
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.large("Уведомления"),
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
                                contentDescription = "Стрелка назад"
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
                                    .menuAnchor(),
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
                                    contentDescription = "Стрелка вниз",
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
            Text("Всё прочтитано")
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
            Text("Удалить всё")
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
                contentDescription = "Иконка категории",
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
                .clip(shape2)
                .clickable(onClick = onClick),
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
                Text("Да")
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
                Text("Нет")
            }
        },
        title = {
            Text("Вы уверены?")
        },
        text = {
            Text("Вы точно хотите ${component.actionName}?")
        }
    )
}

private fun getCategoryName(category: NotificationType): String {
    return when(category) {
        NotificationType.ALL_NOTIFICATIONS -> "Все уведомления"
        NotificationType.DISCUSSION_IN_COMMENTS -> "Обсуждения в отзывах работы"
        NotificationType.UPDATES_FROM_SUBSCRIBED_AUTHORS -> "Обновления у авторов, на которых подписан"
        NotificationType.NEW_WORKS_IN_COLLECTIONS -> "Новые работы в сборниках"
        NotificationType.UPDATES_IN_FANFICS -> "Новые части в работе"
        NotificationType.NEW_BLOGS -> "Оповещения о новых блогах"
        NotificationType.NEW_COMMENTS -> "Новые отзывы"
        NotificationType.SYSTEM_MESSAGES -> "Системные сообщения"
        NotificationType.NEW_WORKS_FOR_LIKED_REQUESTS -> "Новые работы по понравившимся заявкам"
        NotificationType.HELPDESK_RESPONSES -> "Ответы службы поддержки"
        NotificationType.TEXT_CHANGES_IN_OWN_FANFIC -> ""
        NotificationType.NEW_PRESENTS -> "Новые подарки"
        NotificationType.NEW_ACHIEVEMENTS -> "Новые достижения"
        NotificationType.TEXT_CHANGES_IN_EDITED_FANFIC -> "Изменения в тексте работы, где я бета/гамма/соавтор"
        NotificationType.ERROR_MESSAGES -> "Сообщения об ошибках"
        NotificationType.REQUESTS_FOR_COAUTHORSHIPS -> "Запросы на соавторство"
        NotificationType.REQUESTS_FOR_BETA_EDITING -> "Запросы на бету"
        NotificationType.REQUESTS_FOR_GAMMA_EDITING -> "Запросы на гамму"
        NotificationType.NEW_WORKS_ON_MY_REQUESTS -> "Новые работы по моим заявкам"
        NotificationType.DISCUSSION_IN_REQUEST_COMMENTS -> "Обсуждения в отзывах заявки"
        NotificationType.PRIVATE_MESSAGES -> "Личные сообщения"
        NotificationType.NEW_COMMENTS_FOR_REQUEST -> "Новые отзывы к заявке"
        NotificationType.NEW_FANFICS_REWARDS -> "Новые награды к фанфикам"
        NotificationType.NEW_COMMENTS_REWARDS -> "Новые награды к отзывам"
        NotificationType.CHANGES_IN_HEADER_OF_WORK -> "Изменения в шапке работы"
        NotificationType.COAUTHOR_ADD_NEW_CHAPTER -> "Соавтор добавил новую главу в работу, где я автор"
        NotificationType.UNKNOWN -> "Неизвестный раздел"
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