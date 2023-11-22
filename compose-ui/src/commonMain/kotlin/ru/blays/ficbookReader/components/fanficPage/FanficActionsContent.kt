package ru.blays.ficbookReader.components.fanficPage

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.example.myapplication.compose.Res
import ru.blays.ficbookReader.shared.ui.fanficPageComponents.declaration.FanficPageActionsComponent

@Composable
fun FanficActionsContent(
    component: FanficPageActionsComponent
) {
    val state by component.state.subscribeAsState()
    val (follow, mark) = state

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
    val commentsItemIcon = painterResource(Res.image.ic_comment)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FanficActionItem(
            modifier = Modifier.weight(.33333F),
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
            modifier = Modifier.weight(.33333F),
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
            modifier = Modifier.weight(.33333F),
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