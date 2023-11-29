package ru.blays.ficbookReader.components.fanficPage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.example.myapplication.compose.Res
import ru.blays.ficbookReader.shared.data.dto.AvailableCollectionsModel
import ru.blays.ficbookReader.shared.ui.fanficPageComponents.declaration.FanficPageActionsComponent
import ru.blays.ficbookReader.shared.ui.fanficPageComponents.declaration.FanficPageCollectionsComponent
import ru.blays.ficbookReader.theme.lockColor
import ru.blays.ficbookReader.theme.unlockColor
import ru.blays.ficbookReader.values.CardShape
import ru.blays.ficbookReader.values.DefaultPadding

@Composable
fun FanficActionsContent(
    component: FanficPageActionsComponent
) {
    val state by component.state.subscribeAsState()
    val follow = state.follow
    val mark = state.mark

    val childSlot by component.slot.subscribeAsState()

    val likeItemIcon = if (mark) {
        painterResource(Res.image.ic_like_filled)
    } else {
        painterResource(Res.image.ic_like_outlined)
    }
    val subscribeItemIcon = if (follow) {
        painterResource(Res.image.ic_star_filled)
    } else {
        painterResource(Res.image.ic_star_outlined)
    }
    val collectionsItemIcon = painterResource(Res.image.ic_bookmark_outlined)
    val commentsItemIcon = painterResource(Res.image.ic_comment)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FanficActionItem(
            modifier = Modifier.weight(1F/4),
            value = mark,
            icon = likeItemIcon,
            title = "Нравится",
            iconSize = 28.dp,
            iconColor = MaterialTheme.colorScheme.primary
        ) {
            component.sendIntent(
                FanficPageActionsComponent.Intent.Mark(it)
            )
        }
        VerticalDivider()
        FanficActionItem(
            modifier = Modifier.weight(1F/4),
            value = follow,
            icon = subscribeItemIcon,
            title = "Избранное",
            iconSize = 28.dp,
            iconColor = MaterialTheme.colorScheme.primary
        ) {
            component.sendIntent(
                FanficPageActionsComponent.Intent.Follow(it)
            )
        }
        VerticalDivider()
        FanficActionItem(
            modifier = Modifier.weight(1F/4),
            value = true,
            icon = collectionsItemIcon,
            title = "В сборник",
            iconSize = 28.dp,
            iconColor = MaterialTheme.colorScheme.primary
        ) {
            component.sendIntent(
                FanficPageActionsComponent.Intent.OpenAvailableCollections
            )
        }
        VerticalDivider()
        FanficActionItem(
            modifier = Modifier.weight(1F/4),
            value = true,
            icon = commentsItemIcon,
            title = "Отзывы",
            iconSize = 28.dp,
            iconColor = MaterialTheme.colorScheme.primary
        ) {
            component.onOutput(
                FanficPageActionsComponent.Output.OpenComments
            )
        }
    }

    childSlot.child?.instance?.let { slotComponent ->
        val slotState by slotComponent.state.subscribeAsState()
        val availableCollections = slotState.availableCollections?.data?.collections
        if(!slotState.loading) {
            Dialog(
                onDismissRequest = {
                    component.sendIntent(
                        FanficPageActionsComponent.Intent.CloseAvailableCollections
                    )
                }
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Text(
                        text = "Добавить работу в сборник",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(3.dp)
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp),)
                    if (availableCollections != null) {
                        LazyColumn(
                            modifier = Modifier.padding(3.dp),
                        ) {
                            items(availableCollections) { collection ->
                                CollectionItem(
                                    collection = collection,
                                    selected = collection.isInThisCollection == AvailableCollectionsModel.Data.Collection.IN_COLLECTION,
                                    onSelect = { add ->
                                        slotComponent.sendIntent(
                                            FanficPageCollectionsComponent.Intent.AddToCollection(
                                                add = add,
                                                collection = collection
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FanficActionItem(
    modifier: Modifier = Modifier,
    value: Boolean,
    icon: Painter,
    iconColor: Color = Color.Unspecified,
    iconSize: Dp = 24.dp,
    title: String,
    contentDescription: String? = null,
    enabled: Boolean = true,
    onClick: (Boolean) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardDefaults.shape)
            .toggleable(
                value = value,
                enabled = enabled,
                onValueChange = onClick
            )
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.size(iconSize),
            painter = icon,
            contentDescription = contentDescription,
            tint = iconColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun CollectionItem(
    collection: AvailableCollectionsModel.Data.Collection,
    selected: Boolean,
    onSelect: (select: Boolean) -> Unit
) {
    val indicatorIcon = if(collection.isPublic) {
        painterResource(Res.image.ic_unlock)

    } else {
        painterResource(Res.image.ic_lock)
    }
    val indicatorIconColor = if(collection.isPublic) unlockColor else lockColor

    Box(
        modifier = Modifier
            .padding(DefaultPadding.CardDefaultPadding)
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CardDefaults.shape
            )
            .clip(CardDefaults.shape)
            .let {
                if(selected) it.border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = CardDefaults.shape
                ) else it
            }
            .toggleable(
                value = selected,
                onValueChange = onSelect
            ),
    ) {
        Row(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
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
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
                    text = collection.count.toString(),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}