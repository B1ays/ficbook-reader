package ru.blays.ficbook.components.fanficPage

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ficbook_reader.compose_ui.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.reader.shared.components.fanficPageComponents.declaration.FanficPageActionsComponent
import ru.blays.ficbook.reader.shared.components.fanficPageComponents.declaration.FanficPageCollectionsComponent
import ru.blays.ficbook.reader.shared.data.dto.AvailableCollectionsModel
import ru.blays.ficbook.ui_components.spacers.HorizontalSpacer
import ru.blays.ficbook.ui_components.spacers.VerticalSpacer
import ru.blays.ficbook.utils.thenIf
import ru.blays.ficbook.values.CardShape
import ru.blays.ficbook.values.DefaultPadding

@Composable
fun FanficActionsContent(
    component: FanficPageActionsComponent
) {
    val state by component.state.subscribeAsState()
    val follow = state.follow
    val mark = state.mark

    val childSlot by component.slot.subscribeAsState()

    val likeItemIcon = if (mark) {
        painterResource(Res.drawable.ic_like_filled)
    } else {
        painterResource(Res.drawable.ic_like_outlined)
    }
    val subscribeItemIcon = if (follow) {
        painterResource(Res.drawable.ic_star_filled)
    } else {
        painterResource(Res.drawable.ic_star_outlined)
    }
    val collectionsItemIcon = painterResource(Res.drawable.ic_bookmark_outlined)
    val commentsItemIcon = painterResource(Res.drawable.ic_comment)

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
            title = stringResource(Res.string.liked),
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
            title = stringResource(Res.string.favourite),
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
            title = stringResource(Res.string.fanficPage_action_to_collection),
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
            title = stringResource(Res.string.comments),
            iconSize = 28.dp,
            iconColor = MaterialTheme.colorScheme.primary
        ) {
            component.onOutput(
                FanficPageActionsComponent.Output.OpenComments
            )
        }
    }

    childSlot.child?.instance?.let { slotComponent ->
        CollectionsDialog(
            component = slotComponent,
            onDismissRequest = {
                component.sendIntent(
                    FanficPageActionsComponent.Intent.CloseAvailableCollections
                )
            }
        )
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
private fun CollectionsDialog(
    component: FanficPageCollectionsComponent,
    onDismissRequest: () -> Unit
) {
    val state by component.state.subscribeAsState()
    val availableCollections = state.availableCollections?.data?.collections
    val loading = state.loading

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card {
            if(!loading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(DefaultPadding.CardDefaultPadding)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_stack_plus),
                        contentDescription = stringResource(Res.string.content_description_icon_stack_plus),
                        modifier = Modifier.size(24.dp),
                    )
                    HorizontalSpacer(6.dp)
                    Text(
                        text = stringResource(Res.string.fanficPage_collections_dialog_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(3.dp)
                    )
                }
                if (availableCollections != null) {
                    LazyColumn {
                        items(availableCollections) { collection ->
                            CollectionItem(
                                collection = collection,
                                selected = collection.isInThisCollection == AvailableCollectionsModel.Data.Collection.IN_COLLECTION,
                                onSelect = { add ->
                                    component.sendIntent(
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
                VerticalSpacer(6.dp)
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(DefaultPadding.CardDefaultPadding)
                ) {
                    CircularProgressIndicator()
                    HorizontalSpacer(10.dp)
                    Text(
                        text = stringResource(Res.string.loading_in_progress),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun CollectionItem(
    modifier: Modifier = Modifier,
    collection: AvailableCollectionsModel.Data.Collection,
    selected: Boolean,
    onSelect: (select: Boolean) -> Unit
) {
    val indicatorIcon = if(collection.isPublic) {
        painterResource(Res.drawable.ic_unlock)

    } else {
        painterResource(Res.drawable.ic_lock)
    }

    Box(
        modifier = modifier
            .padding(DefaultPadding.CardDefaultPadding)
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                shape = CardDefaults.shape
            )
            .clip(CardDefaults.shape)
            .thenIf(selected) {
                border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = CardDefaults.shape
                )
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
                    contentDescription = stringResource(Res.string.content_description_icon_lock),
                    tint = MaterialTheme.colorScheme.primary,
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
                    .height(36.dp)
                    .widthIn(min = 36.dp, max = 50.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        CardShape.CardStandalone,
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = collection.count.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }
        }
    }
}