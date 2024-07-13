package ru.blays.ficbook.components.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import ficbook_reader.compose_ui.generated.resources.Res
import ficbook_reader.compose_ui.generated.resources.ic_arrow_down
import ficbook_reader.compose_ui.generated.resources.ic_arrow_forward
import org.jetbrains.compose.resources.painterResource
import ru.blays.ficbook.ui_components.CustomButton.BackgroundedIcon
import ru.blays.ficbook.ui_components.CustomShape.SquircleShape.CornerSmoothing
import ru.blays.ficbook.ui_components.CustomShape.SquircleShape.SquircleShape
import ru.blays.ficbook.ui_components.spacers.HorizontalSpacer
import ru.blays.ficbook.utils.surfaceColorAtAlpha
import ru.blays.ficbook.values.CardShape
import ru.blays.ficbook.values.DefaultPadding
import java.lang.String.format

const val ANIMATION_DURATION_MILLIS = 300

@Composable
fun SettingsClickableCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    title: String,
    subtitle: String? = null,
    icon: Painter? = null,
    shape: Shape = CardDefaults.shape,
) {
    ElevatedCard(
        modifier = modifier
            .padding(DefaultPadding.CardPaddingSmallVertical)
            .fillMaxWidth(),
        shape = shape,
        enabled = enabled,
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if(icon != null) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(50.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = SquircleShape(
                                cornerSmoothing = CornerSmoothing.High
                            ),
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(.6F),
                        modifier = Modifier.fillMaxSize(0.5F)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.7F)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (subtitle != null) Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_forward),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
fun SettingsExpandableCard(
    title: String,
    subtitle: String? = null,
    icon: Painter? = null,
    shape: Shape = CardDefaults.shape,
    content: @Composable ColumnScope.() -> Unit
) {
    var isCardExpanded by rememberSaveable { mutableStateOf(false) }

    val transition = updateTransition(
        targetState = isCardExpanded,
        label = null
    )
    val rotateValue by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = ANIMATION_DURATION_MILLIS)
        }
    ) { expanded ->
        if (expanded) 180F else 0F
    }

    ElevatedCard(
        modifier = Modifier
            .padding(DefaultPadding.CardPaddingSmallVertical)
            .fillMaxWidth()
            .clip(shape)
            .toggleable(
                value = isCardExpanded
            ) { newValue ->
                isCardExpanded = newValue
            },
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedContent(
                targetState = icon,
                transitionSpec = {
                    fadeIn() togetherWith  fadeOut()
                }
            ) {
                if(it != null) {
                    BackgroundedIcon(
                        modifier = Modifier.padding(horizontal = 6.dp),
                        icon = it,
                        iconScale = 0.8F,
                        shape = SquircleShape(
                            cornerSmoothing = CornerSmoothing.High
                        ),
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.primary.copy(.6F)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Column(
                    modifier = Modifier.fillMaxWidth(0.7F)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (subtitle != null) Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotateValue),
                    painter = painterResource(Res.drawable.ic_arrow_down),
                    contentDescription = null
                )
            }
        }
        transition.AnimatedVisibility(
            visible = { it },
            enter = expandVertically(
                animationSpec = spring(stiffness = 300F, dampingRatio = .6F)
            ),
            exit = shrinkVertically(
                animationSpec = spring(stiffness = 300F, dampingRatio = .6F)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = content
            )
        }
    }
}

@Composable
fun SettingsExpandableCard(
    title: String,
    subtitle: String = "",
    icon: ImageVector? = null,
    shape: Shape = CardDefaults.shape,
    content: @Composable ColumnScope.() -> Unit
) = SettingsExpandableCard(
    title = title,
    subtitle = subtitle,
    icon = icon?.let { rememberVectorPainter(it) },
    shape = shape,
    content = content
)

@Composable
fun SettingsCardWithSwitch(
    title: String,
    subtitle: String,
    icon: Painter? = null,
    shape: Shape = CardDefaults.shape,
    enabled: Boolean,
    isSwitchEnabled: Boolean = true,
    action: (Boolean) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .padding(DefaultPadding.CardPaddingSmallVertical)
            .clip(shape),
        onClick = { action(!enabled) },
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                BackgroundedIcon(
                    modifier = Modifier.padding(horizontal = 6.dp),
                    icon = icon,
                    iconScale = 0.8F,
                    shape = SquircleShape(
                        cornerSmoothing = CornerSmoothing.High
                    ),
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary.copy(.6F)
                )
            }
            Row(
                modifier = Modifier
                    .padding(vertical = 5.dp, horizontal = 4.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.7F)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = null,
                    enabled = isSwitchEnabled
                )
            }
        }
    }
}

@Composable
fun SettingsRadioButtonWithTitle(
    title: String,
    checkedIndex: Int,
    enabled: Boolean = true,
    index: Int,
    action: (index: Int) -> Unit
) {
    Box(
        modifier = Modifier
            .padding(DefaultPadding.CardDefaultPadding)
            .fillMaxWidth()
            .clip(CardShape.CardStandalone)
            .clickable(
                enabled = enabled,
                onClick = { action(index) }
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                vertical = 4.dp
            ),
        ) {
            RadioButton(
                selected = checkedIndex == index,
                onClick = null,
                enabled = enabled
            )
            HorizontalSpacer(8.dp)
            Text(text = title)
        }
    }
}

@Composable
fun SettingsCheckboxWithTitle(
    title: String,
    state: Boolean,
    enabled: Boolean = true,
    action: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(
                vertical = 2.dp,
                horizontal = 12.dp
            )
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    )
    {
        Text(text = title)
        Checkbox(
            checked = state,
            onCheckedChange = action,
            enabled = enabled
        )
    }
}

@Composable
fun SettingsSwitchWithTitle(
    title: String,
    state: Boolean,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    action: (Boolean) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape.CardStandalone)
            .toggleable(
                value = state,
                onValueChange = action,
                enabled = enabled
            ),
    ) {
        Row(
            modifier = Modifier.padding(
                vertical = 4.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1F),
                text = title
            )
            Spacer(modifier = Modifier.width(6.dp))
            Switch(
                checked = state,
                onCheckedChange = null,
                enabled = enabled
            )
        }
    }
}

@Composable
fun SettingsSliderWithTitle(
    title: String,
    enabled: Boolean,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = Float.MIN_VALUE..Float.MAX_VALUE,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(
                vertical = 2.dp,
                horizontal = 12.dp
            )
            .fillMaxWidth()
    ) {
        Text(text = title)
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Slider(
                value = value,
                valueRange = valueRange,
                onValueChange = onValueChange,
                enabled = enabled,
                colors = SliderDefaults.colors(
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceColorAtAlpha(0.2F)
                ),
                modifier = Modifier.weight(1F),
            )
            HorizontalSpacer(6.dp)
            Text(format("%.2f", value))
        }
    }
}

@Composable
fun ColorPickerItem(
    modifier: Modifier = Modifier,
    color: Color,
    index: Int,
    selectedItemIndex: Int?,
    actionSelectColor: (Int) -> Unit
) {
    Box(
        modifier = modifier
            .size(50.dp)
            .padding(4.dp)
            .clip(CircleShape)
            .background(color = color)
            .clickable { actionSelectColor(index) }
            .then(
                if (selectedItemIndex == index) Modifier.border(
                    width = 3.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = CircleShape
                ) else Modifier
            )
    )
}