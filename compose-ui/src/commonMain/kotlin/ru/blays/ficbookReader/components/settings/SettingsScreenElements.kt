package ru.blays.ficbookReader.components.settings

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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.compose.Res
import ru.blays.ficbookReader.ui_components.CustomButton.BackgroundIcon
import ru.blays.ficbookReader.values.CardShape
import ru.blays.ficbookReader.values.DefaultPadding

const val ANIMATION_DURATION_MILLIS = 300

@Composable
fun SettingsExpandableCard(
    title: String,
    subtitle: String = "",
    icon: Painter? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    var isCardExpanded by rememberSaveable { mutableStateOf(false) }

    val transition = updateTransition(targetState = isCardExpanded, label = null)
    val rotateValue by transition.animateFloat(
        transitionSpec = {
            tween(
                durationMillis = ANIMATION_DURATION_MILLIS
            )
        }
    ) { expanded ->
        if (expanded) 180F else 0F
    }

    Card(
        modifier = Modifier
            .padding(DefaultPadding.CardDefaultPadding)
            .fillMaxWidth()
            .clip(CardShape.CardStandalone)
            .toggleable(
                value = isCardExpanded
            ) { newValue ->
                isCardExpanded = newValue
            },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedContent(
                targetState = icon,
            ) {
                if(it != null) {
                    BackgroundIcon(
                        modifier = Modifier.padding(horizontal = 6.dp),
                        icon = it,
                        iconScale = 0.8F,
                        shape = MaterialTheme.shapes.large,
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
                    modifier = Modifier
                        .fillMaxWidth(0.7F)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (subtitle.isNotEmpty()) Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotateValue),
                    painter = painterResource(Res.image.ic_arrow_down),
                    contentDescription = null
                )
            }
        }
        AnimatedVisibility(
            visible = isCardExpanded,
            enter = slideInVertically(
                animationSpec = spring(stiffness = 300F, dampingRatio = .6F),
                initialOffsetY = { -it / 2 }
            ) + expandVertically(),
            exit = slideOutVertically(
                animationSpec = spring(stiffness = 300F, dampingRatio = .6F),
                targetOffsetY = { -it / 2 }
            ) + shrinkVertically()
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
    content: @Composable ColumnScope.() -> Unit
) = SettingsExpandableCard(
    title = title,
    subtitle = subtitle,
    icon = icon?.let { androidx.compose.ui.graphics.vector.rememberVectorPainter(it) },
    content = content
)

@Composable
fun SettingsCardWithSwitch(
    title: String,
    subtitle: String,
    icon: Painter? = null,
    state: Boolean,
    isSwitchEnabled: Boolean = true,
    action: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(DefaultPadding.CardDefaultPadding)
            .clip(CardShape.CardStandalone)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                BackgroundIcon(
                    modifier = Modifier.padding(horizontal = 6.dp),
                    icon = icon,
                    iconScale = 0.8F,
                    shape = MaterialTheme.shapes.large,
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
                    checked = state,
                    onCheckedChange = action,
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
    Row(
        modifier = Modifier
            .padding(vertical = 2.dp, horizontal = 12.dp)
            .fillMaxWidth()
            .clip(CardShape.CardStandalone)
            .clickable(enabled = enabled, onClick = { action(index) }),
        verticalAlignment = Alignment.CenterVertically
    )
    {
        RadioButton(
            selected = checkedIndex == index,
            onClick = { action(index) },
            enabled = enabled
        )
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = title
        )
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
            .padding(vertical = 2.dp, horizontal = 12.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    )
    {
        Text(modifier = Modifier.padding(start = 8.dp), text = title)
        Checkbox(
            checked = state,
            onCheckedChange = action,
            enabled = enabled
        )
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

/*@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColorPickerItem(
    brush: Brush,
    customColorSelected: Boolean,
    actionSelect: () -> Unit,
    actionOpenDialog: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .padding(4.dp)
            .clip(CircleShape)
            .then(
                if (customColorSelected) Modifier.border(
                    width = 3.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = CircleShape
                ) else Modifier
            )
            .blur(3.dp)
            .combinedClickable(
                onClick = actionSelect,
                onLongClick = actionOpenDialog
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(brush = brush)
        )
    }
}*/

@Composable
fun CurrentSegment(
    currentSegment: Segment,
    modifier: Modifier,
    alignment: Alignment
) {
    Box(modifier = modifier, contentAlignment = alignment) {
        AnimatedContent(
            targetState = currentSegment,
            transitionSpec = {
                if (targetState.start > initialState.start) {
                    (slideInVertically { height -> height } + fadeIn()) togetherWith(
                        slideOutVertically { height -> -height } + fadeOut())
                } else {
                    (slideInVertically { height -> -height } + fadeIn()) togetherWith(
                        slideOutVertically { height -> height } + fadeOut())
                }.using(
                    SizeTransform(clip = false)
                )
            }
        ) { currentSegment ->
            Text(
                text = currentSegment.name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

/*@Composable
fun ColorPickerDialogContent(
    color: Color,
    onClose: () -> Unit,
    onPick: (color: Color) -> Unit
) {
    val controller = rememberColorPickerController()

    val selectedColor by controller.selectedColor

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        CustomIconButton(
            onClick = onClose,
            minSize = 30.dp,
            containerColor = MaterialTheme.colorScheme.surfaceColorAtAlpha(0.3F),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(fanficID = R.drawable.round_close_24),
                contentDescription = null
            )
        }
    }
    HsvColorPicker(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(10.dp),
        controller = controller,
        initialColor = color
    )
    Spacer(modifier = Modifier.height(8.dp))
    BrightnessSlider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .height(35.dp),
        controller = controller,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Button(
            modifier = Modifier
                .height(65.dp)
                .wrapContentWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                contentColor = selectedColor.invert(),
                containerColor = selectedColor
            ),
            onClick = {
                onPick(selectedColor)
                onClose()
            },

            ) {
            Text(
                text = stringResource(fanficID = R.string.Pick_color),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}*/

data class Segment(
    val start: Float,
    val name: String
)