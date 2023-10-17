package ru.blays.ficbookReader.ui_components.Scrollbar

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun VerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier,
    reverseLayout: Boolean,
    interactionSource: MutableInteractionSource
) {}

@Composable
actual fun VerticalScrollbar(
    lazyListState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier,
    reverseLayout: Boolean,
    interactionSource: MutableInteractionSource
) {}

@Composable
actual fun HorizontalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier,
    reverseLayout: Boolean,
    interactionSource: MutableInteractionSource
) {}

@Composable
actual fun HorizontalScrollbar(
    lazyListState: LazyListState,
    modifier: Modifier,
    reverseLayout: Boolean,
    interactionSource: MutableInteractionSource
) {}