@file:OptIn(ExperimentalResourceApi::class)

package ru.blays.ficbook.ui_components.FanficComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ficbook_reader.`compose-ui`.generated.resources.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import ru.blays.ficbook.reader.shared.data.dto.FanficTagStable

@Composable
fun CircleChip(
    modifier: Modifier = Modifier,
    color: Color,
    minSize: Dp = 30.dp,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .padding(2.dp)
            .background(color, CircleShape)
            .padding(3.dp)
            .defaultMinSize(
                minHeight = minSize,
                minWidth = minSize
            )
            .clip(CircleShape)
            .then(modifier),
        content = content,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    )
}

@Composable
fun CircleChip(
    modifier: Modifier = Modifier,
    color: Color,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .padding(4.dp)
            .background(color, CircleShape)
            .padding(3.dp)
            .clip(CircleShape)
            .then(modifier),
        content = content,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    )
}

@Composable
fun FanficTagChip(
    tag: FanficTagStable,
    onClick: () -> Unit = {}
) {
    CircleChip(
        modifier = Modifier
            .height(20.dp)
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.primary
    ) {
        Text(
            text = tag.name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
        if(tag.isAdult) {
            Spacer(modifier = Modifier.width(3.dp))
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(Res.drawable.ic_18),
                contentDescription = "Иконка 18+",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}