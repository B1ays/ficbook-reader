package ru.blays.ficbook.reader.desktop

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import ficbook_reader.`app-desktop`.generated.resources.Res
import ficbook_reader.`app-desktop`.generated.resources.ic_cross
import ficbook_reader.`app-desktop`.generated.resources.ic_minimize
import ficbook_reader.`app-desktop`.generated.resources.ic_restore
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun FrameWindowScope.WindowFrame(
    state: WindowState,
    onClose: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val iconActiveColor = MaterialTheme.colorScheme.primary
    val iconInactiveColor = MaterialTheme.colorScheme.primary.copy(0.7F)
    val frameShape = RoundedCornerShape(
        topStart = 12.dp,
        topEnd = 12.dp
    )
    Column {
        WindowDraggableArea {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = frameShape
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ficbook reader",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                Row {
                    IconButton(
                        modifier = Modifier.fillMaxHeight(),
                        onClick = { window.isMinimized = true }
                    ) {
                        CursorListenedIcon(
                            modifier = Modifier.size(16.dp),
                            painter = painterResource(Res.drawable.ic_minimize),
                            contentDescription = null,
                            activeTint = iconActiveColor,
                            inactiveTint = iconInactiveColor
                        )
                    }
                    IconButton(
                        modifier = Modifier.fillMaxHeight(),
                        onClick = {
                            if(state.placement == WindowPlacement.Floating) {
                                state.placement = WindowPlacement.Maximized
                            } else {
                                state.placement = WindowPlacement.Floating
                            }
                        }
                    ) {
                        CursorListenedIcon(
                            modifier = Modifier.size(16.dp),
                            painter = painterResource(Res.drawable.ic_restore),
                            contentDescription = null,
                            activeTint = iconActiveColor,
                            inactiveTint = iconInactiveColor
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.fillMaxHeight(),
                    ) {
                        CursorListenedIcon(
                            modifier = Modifier.size(16.dp),
                            painter = painterResource(Res.drawable.ic_cross),
                            contentDescription = null,
                            activeTint = iconActiveColor,
                            inactiveTint = iconInactiveColor
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }
            }
        }
        content()
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CursorListenedIcon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    activeTint: Color,
    inactiveTint: Color
) {
    var isActive by remember { mutableStateOf(false) }
    val tint by animateColorAsState(if (isActive) activeTint else inactiveTint)
    Icon(
        painter = painter,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier
            .onPointerEvent(PointerEventType.Enter) { isActive = true }
            .onPointerEvent(PointerEventType.Exit) { isActive = false },
    )
}