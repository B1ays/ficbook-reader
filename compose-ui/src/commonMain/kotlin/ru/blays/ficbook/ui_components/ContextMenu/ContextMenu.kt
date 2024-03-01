package ru.blays.ficbook.ui_components.ContextMenu

import androidx.compose.animation.core.*
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup

class ContextMenuState {
    private var _visible: Boolean by mutableStateOf(false)
    private var _position: Offset by mutableStateOf(Offset.Unspecified)
    private var _size: IntSize by mutableStateOf(IntSize.Zero)

    val visible: Boolean get() = _visible
    val position: Offset get() = _position
    val size: IntSize get() = _size

    fun setSize(size: IntSize) {
        _size = size
    }

    fun setPosition(position: Offset) {
        _position = position
        //println("Position: $position")
    }

    fun show() {
        _visible = true
        println("Show")
    }

    fun hide() {
        _visible = false
        println("Hide")
    }
}

@Composable
fun rememberContextMenuState(): ContextMenuState {
    return remember { ContextMenuState() }
}

@Composable
fun ContextMenu(
    state: ContextMenuState,
    scrollState: ScrollState = rememberScrollState(),
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val expandedState = remember { MutableTransitionState(false) }
    expandedState.targetState = state.visible

    if(expandedState.currentState || expandedState.targetState) {
        val density = LocalDensity.current
        val popupPositionProvider = remember(state.position, state.size) {
            ContextMenuPositionProvider(
                positionInWindow = state.position.toIntOffset(),
                size = state.size,
                offsetFromEdge = with(density) { 12.dp.roundToPx() }
            )
        }

        Popup(
            popupPositionProvider = popupPositionProvider,
            onDismissRequest = state::hide
        ) {
            DropdownMenuContent(
                expandedState = expandedState,
                transformOriginState = remember { mutableStateOf(TransformOrigin.Center) },
                scrollState = scrollState,
                content = content
            )
        }
    }
}

@Suppress("ModifierParameter")
@Composable
fun DropdownMenuContent(
    expandedState: MutableTransitionState<Boolean>,
    transformOriginState: MutableState<TransformOrigin>,
    scrollState: ScrollState,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    // Menu open/close animation.
    val transition = updateTransition(expandedState, "DropDownMenu")

    val scale by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to expanded
                tween(
                    durationMillis = InTransitionDuration,
                    easing = LinearOutSlowInEasing
                )
            } else {
                // Expanded to dismissed.
                tween(
                    durationMillis = 1,
                    delayMillis = OutTransitionDuration - 1
                )
            }
        }
    ) { expanded ->
        if (expanded) 1f else 0.8f
    }

    val alpha by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to expanded
                tween(durationMillis = 30)
            } else {
                // Expanded to dismissed.
                tween(durationMillis = OutTransitionDuration)
            }
        }
    ) { expanded ->
        if (expanded) 1f else 0f
    }

    Surface(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
            transformOrigin = transformOriginState.value
        },
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
    ) {
        Column(
            modifier = modifier
                .padding(vertical = DropdownMenuVerticalPadding)
                .width(IntrinsicSize.Max)
                .verticalScroll(scrollState), content = content
        )
    }
}

// Size defaults.
internal val MenuVerticalMargin = 48.dp
private val DropdownMenuItemHorizontalPadding = 12.dp
internal val DropdownMenuVerticalPadding = 8.dp
private val DropdownMenuItemDefaultMinWidth = 112.dp
private val DropdownMenuItemDefaultMaxWidth = 280.dp

// Menu open/close animation.
internal const val InTransitionDuration = 120
internal const val OutTransitionDuration = 75