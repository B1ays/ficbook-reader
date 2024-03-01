package ru.blays.ficbook.ui_components.Scrollbar

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
private fun defaultScrollbarStyle(): ScrollbarStyle {
    val unhoverColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
    val hoverColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.50f)
    return ScrollbarStyle(
        minimalHeight = 16.dp,
        thickness = 10.dp,
        shape = RoundedCornerShape(4.dp),
        hoverDurationMillis = 300,
        unhoverColor = unhoverColor,
        hoverColor = hoverColor
    )
}

@Composable
actual fun VerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier,
    reverseLayout: Boolean,
    interactionSource: MutableInteractionSource
) = androidx.compose.foundation.VerticalScrollbar(
    adapter = rememberScrollbarAdapter(scrollState),
    modifier = modifier,
    reverseLayout = reverseLayout,
    style = defaultScrollbarStyle(),
    interactionSource = interactionSource
)

@Composable
actual fun VerticalScrollbar(
    lazyListState: LazyListState,
    modifier: Modifier,
    reverseLayout: Boolean,
    interactionSource: MutableInteractionSource
) = androidx.compose.foundation.VerticalScrollbar(
    adapter = rememberScrollbarAdapter(lazyListState),
    modifier = modifier,
    reverseLayout = reverseLayout,
    style = defaultScrollbarStyle(),
    interactionSource = interactionSource
)

@Composable
actual fun HorizontalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier,
    reverseLayout: Boolean,
    interactionSource: MutableInteractionSource
) = androidx.compose.foundation.HorizontalScrollbar(
    adapter = rememberScrollbarAdapter(scrollState),
    modifier = modifier,
    reverseLayout = reverseLayout,
    style = defaultScrollbarStyle(),
    interactionSource = interactionSource
)

@Composable
actual fun HorizontalScrollbar(
    lazyListState: LazyListState,
    modifier: Modifier,
    reverseLayout: Boolean,
    interactionSource: MutableInteractionSource
) = androidx.compose.foundation.HorizontalScrollbar(
    adapter = rememberScrollbarAdapter(lazyListState),
    modifier = modifier,
    reverseLayout = reverseLayout,
    style = defaultScrollbarStyle(),
    interactionSource = interactionSource
)