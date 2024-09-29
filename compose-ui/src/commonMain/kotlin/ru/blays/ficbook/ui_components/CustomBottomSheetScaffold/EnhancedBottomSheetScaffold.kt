/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.blays.ficbook.ui_components.CustomBottomSheetScaffold

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ScaffoldDefaults.contentWindowInsets
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.util.*
import kotlinx.coroutines.launch
import ru.blays.ficbook.ui_components.CustomBottomSheetScaffold.SheetValue.*
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * <a href="https://m3.material.io/components/bottom-sheets/overview" class="external"
 * target="_blank">Material Design standard bottom sheet scaffold</a>.
 *
 * Standard bottom sheets co-exist with the screen’s main UI region and allow for simultaneously
 * viewing and interacting with both regions. They are commonly used to keep a feature or secondary
 * content visible on screen when content in main UI region is frequently scrolled or panned.
 *
 * ![Bottom sheet
 * image](https://developer.android.com/images/reference/androidx/compose/material3/bottom_sheet.png)
 *
 * This component provides API to put together several material components to construct your screen,
 * by ensuring proper layout strategy for them and collecting necessary data so these components
 * will work together correctly.
 *
 * A simple example of a standard bottom sheet looks like this:
 *
 * @sample androidx.compose.material3.samples.SimpleBottomSheetScaffoldSample
 *
 * @param sheetContent the content of the bottom sheet
 * @param modifier the [Modifier] to be applied to this scaffold
 * @param scaffoldState the state of the bottom sheet scaffold
 * @param sheetPeekHeight the height of the bottom sheet when it is collapsed
 * @param sheetMaxWidth [Dp] that defines what the maximum width the sheet will take. Pass in
 *   [Dp.Unspecified] for a sheet that spans the entire screen width.
 * @param sheetShape the shape of the bottom sheet
 * @param sheetContainerColor the background color of the bottom sheet
 * @param sheetContentColor the preferred content color provided by the bottom sheet to its
 *   children. Defaults to the matching content color for [sheetContainerColor], or if that is not a
 *   color from the theme, this will keep the same content color set above the bottom sheet.
 * @param sheetTonalElevation when [sheetContainerColor] is [ColorScheme.surface], a translucent
 *   primary color overlay is applied on top of the container. A higher tonal elevation value will
 *   result in a darker color in light theme and lighter color in dark theme. See also: [Surface].
 * @param sheetShadowElevation the shadow elevation of the bottom sheet
 * @param sheetDragHandle optional visual marker to pull the scaffold's bottom sheet
 * @param sheetSwipeEnabled whether the sheet swiping is enabled and should react to the user's
 *   input
 * @param topBar top app bar of the screen, typically a [SmallTopAppBar]
 * @param snackbarHost component to host [Snackbar]s that are pushed to be shown via
 *   [SnackbarHostState.showSnackbar], typically a [SnackbarHost]
 * @param containerColor the color used for the background of this scaffold. Use [Color.Transparent]
 *   to have no color.
 * @param contentColor the preferred color for content inside this scaffold. Defaults to either the
 *   matching content color for [containerColor], or to the current [LocalContentColor] if
 *   [containerColor] is not a color from the theme.
 * @param content content of the screen. The lambda receives a [PaddingValues] that should be
 *   applied to the content root via [Modifier.padding] and [Modifier.consumeWindowInsets] to
 *   properly offset top and bottom bars. If using [Modifier.verticalScroll], apply this modifier to
 *   the child of the scroll, and not on the scroll itself.
 */
@Composable
fun EnhancedBottomSheetScaffold(
    sheetContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    sheetPeekHeight: Dp = BottomSheetDefaults.SheetPeekHeight,
    sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
    sheetShape: Shape = BottomSheetDefaults.ExpandedShape,
    sheetContainerColor: Color = BottomSheetDefaults.ContainerColor,
    sheetContentColor: Color = contentColorFor(sheetContainerColor),
    sheetTonalElevation: Dp = 0.dp,
    sheetShadowElevation: Dp = BottomSheetDefaults.Elevation,
    sheetDragHandle: (@Composable () -> Unit)? = { BottomSheetDefaults.DragHandle() },
    sheetSwipeEnabled: Boolean = true,
    fullscreenSheet: Boolean = false,
    topBar: @Composable (() -> Unit)? = null,
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.Companion.End,
    snackbarHost: @Composable (SnackbarHostState) -> Unit = { SnackbarHost(it) },
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(containerColor),
    content: @Composable (PaddingValues) -> Unit,
) {
    BottomSheetScaffoldLayout(
        modifier = modifier,
        topBar = topBar,
        body = { content(PaddingValues(bottom = sheetPeekHeight)) },
        snackbarHost = { snackbarHost(scaffoldState.snackbarHostState) },
        sheetOffset = { scaffoldState.bottomSheetState.requireOffset() },
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        sheetState = scaffoldState.bottomSheetState,
        containerColor = containerColor,
        contentColor = contentColor,
        bottomSheet = { layoutHeight ->
            StandardBottomSheet(
                state = scaffoldState.bottomSheetState,
                peekHeight = sheetPeekHeight,
                layoutHeight = layoutHeight,
                sheetMaxWidth = sheetMaxWidth,
                sheetSwipeEnabled = sheetSwipeEnabled,
                fullscreen = fullscreenSheet,
                shape = sheetShape,
                containerColor = sheetContainerColor,
                contentColor = sheetContentColor,
                tonalElevation = sheetTonalElevation,
                shadowElevation = sheetShadowElevation,
                dragHandle = sheetDragHandle,
                content = sheetContent
            )
        }
    )
}

/**
 * State of the [BottomSheetScaffold] composable.
 *
 * @param bottomSheetState the state of the persistent bottom sheet
 * @param snackbarHostState the [SnackbarHostState] used to show snackbars inside the scaffold
 */
@ExperimentalMaterial3Api
@Stable
class BottomSheetScaffoldState(
    val bottomSheetState: SheetState,
    val snackbarHostState: SnackbarHostState,
)

/**
 * Create and [remember] a [BottomSheetScaffoldState].
 *
 * @param bottomSheetState the state of the standard bottom sheet. See
 *   [rememberStandardBottomSheetState]
 * @param snackbarHostState the [SnackbarHostState] used to show snackbars inside the scaffold
 */
@Composable
@ExperimentalMaterial3Api
fun rememberBottomSheetScaffoldState(
    bottomSheetState: SheetState = rememberStandardBottomSheetState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
): BottomSheetScaffoldState {
    return remember(bottomSheetState, snackbarHostState) {
        BottomSheetScaffoldState(
            bottomSheetState = bottomSheetState,
            snackbarHostState = snackbarHostState
        )
    }
}

/**
 * Create and [remember] a [SheetState] for [BottomSheetScaffold].
 *
 * @param initialValue the initial value of the state. Should be either [PartiallyExpanded] or
 *   [Expanded] if [skipHiddenState] is true
 * @param confirmValueChange optional callback invoked to confirm or veto a pending state change
 * @param [skipHiddenState] whether Hidden state is skipped for [BottomSheetScaffold]
 */
@Composable
@ExperimentalMaterial3Api
fun rememberStandardBottomSheetState(
    initialValue: SheetValue = SheetValue.PartiallyExpanded,
    confirmValueChange: (SheetValue) -> Boolean = { true },
    skipHiddenState: Boolean = true,
) =
    rememberSheetState(
        confirmValueChange = confirmValueChange,
        initialValue = initialValue,
        skipHiddenState = skipHiddenState,
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StandardBottomSheet(
    state: SheetState,
    peekHeight: Dp,
    layoutHeight: Int,
    sheetMaxWidth: Dp,
    sheetSwipeEnabled: Boolean,
    fullscreen: Boolean,
    shape: Shape,
    containerColor: Color,
    contentColor: Color,
    tonalElevation: Dp,
    shadowElevation: Dp,
    dragHandle: @Composable (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scope = rememberCoroutineScope()
    val orientation = Orientation.Vertical
    val density = LocalDensity.current
    val peekHeightPx = with(LocalDensity.current) { peekHeight.toPx() }
    val nestedScroll =
        if (sheetSwipeEnabled) {
            Modifier.nestedScroll(
                remember(state.anchoredDraggableState) {
                    ConsumeSwipeWithinBottomSheetBoundsNestedScrollConnection(
                        sheetState = state,
                        orientation = orientation,
                        onFling = { scope.launch { state.settle(it) } }
                    )
                }
            )
        } else {
            Modifier
        }
    Surface(
        modifier = Modifier
            .widthIn(max = sheetMaxWidth)
            .fillMaxWidth()
            .requiredHeightIn(
                min = if(fullscreen) {
                    with(density) { layoutHeight.toDp() }
                } else peekHeight
            )
            .then(nestedScroll)
            .draggableAnchors(
                state.anchoredDraggableState,
                orientation
            ) { sheetSize, constraints ->
                val layoutHeight = constraints.maxHeight.toFloat()
                val sheetHeight = sheetSize.height.toFloat()
                val newAnchors = DraggableAnchors {
                    if (!state.skipPartiallyExpanded) {
                        PartiallyExpanded at (layoutHeight - peekHeightPx)
                    }
                    if (sheetHeight != peekHeightPx) {
                        Expanded at maxOf(layoutHeight - sheetHeight, 0f)
                    }
                    if (!state.skipHiddenState) {
                        Hidden at layoutHeight
                    }
                }
                val newTarget =
                    when(
                        val oldTarget = state.anchoredDraggableState.targetValue
                    ) {
                        Hidden -> if (newAnchors.hasAnchorFor(Hidden)) Hidden else oldTarget
                        PartiallyExpanded ->
                            when {
                                newAnchors.hasAnchorFor(PartiallyExpanded) -> PartiallyExpanded
                                newAnchors.hasAnchorFor(Expanded) -> Expanded
                                newAnchors.hasAnchorFor(Hidden) -> Hidden
                                else -> oldTarget
                            }
                        Expanded ->
                            when {
                                newAnchors.hasAnchorFor(Expanded) -> Expanded
                                newAnchors.hasAnchorFor(PartiallyExpanded) -> PartiallyExpanded
                                newAnchors.hasAnchorFor(Hidden) -> Hidden
                                else -> oldTarget
                            }
                    }
                return@draggableAnchors newAnchors to newTarget
            }
            .anchoredDraggable(
                state = state.anchoredDraggableState,
                orientation = orientation,
                enabled = sheetSwipeEnabled
            ),
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
    ) {
        Column(Modifier.fillMaxWidth()) {
            if (dragHandle != null) {
                val partialExpandActionLabel = "partialExpanded"
                val dismissActionLabel = "dismissAction"
                val expandActionLabel = "expandAction"
                Box(
                    Modifier.align(CenterHorizontally).semantics(mergeDescendants = true) {
                        with(state) {
                            // Provides semantics to interact with the bottomsheet if there is more
                            // than one anchor to swipe to and swiping is enabled.
                            if (state.anchoredDraggableState.anchors.size > 1 && sheetSwipeEnabled) {
                                if (currentValue == PartiallyExpanded) {
                                    if (state.anchoredDraggableState.confirmValueChange(Expanded)) {
                                        expand(expandActionLabel) {
                                            scope.launch { expand() }
                                            true
                                        }
                                    }
                                } else {
                                    if (
                                        state.anchoredDraggableState.confirmValueChange(PartiallyExpanded)
                                    ) {
                                        collapse(partialExpandActionLabel) {
                                            scope.launch { partialExpand() }
                                            true
                                        }
                                    }
                                }
                                if (!state.skipHiddenState) {
                                    dismiss(dismissActionLabel) {
                                        scope.launch { hide() }
                                        true
                                    }
                                }
                            }
                        }
                    },
                ) {
                    dragHandle()
                }
            }
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetScaffoldLayout(
    modifier: Modifier,
    topBar: @Composable (() -> Unit)?,
    body: @Composable () -> Unit,
    bottomSheet: @Composable (layoutHeight: Int) -> Unit,
    snackbarHost: @Composable () -> Unit,
    floatingActionButton: @Composable () -> Unit,
    floatingActionButtonPosition: FabPosition,
    sheetOffset: () -> Float,
    sheetState: SheetState,
    containerColor: Color,
    contentColor: Color,
) {
    val contentWindowInsets = contentWindowInsets

    SubcomposeLayout(
        modifier = modifier
    ) { constraints ->
        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight

        val topBarMeasurables = subcompose(
            slotId = "topBar",
            content = topBar ?: {
                Surface(
                    color = containerColor,
                    contentColor = contentColor,
                    content = body
                )
            }
        )
        val bodyMeasurables = subcompose(
            slotId = "body",
            content = body
        )
        val bottomSheetMeasurables = subcompose(
            slotId = "bottomSheet",
            content = { bottomSheet(layoutHeight) }
        )
        val snackbarHostMeasurables = subcompose(
            slotId = "snackbarHost",
            content = snackbarHost
        )
        val floatingActionButton = subcompose(
            slotId = "floatingActionButton",
            content = floatingActionButton
        )

        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        val sheetPlaceables = bottomSheetMeasurables.fastMap { it.measure(looseConstraints) }

        val topBarPlaceables = topBarMeasurables.fastMap { it.measure(looseConstraints) }
        val topBarHeight = topBarPlaceables.fastMaxOfOrNull { it.height } ?: 0

        val bodyConstraints = looseConstraints.copy(maxHeight = layoutHeight - topBarHeight)
        val bodyPlaceables = bodyMeasurables.fastMap { it.measure(bodyConstraints) }

        val snackbarPlaceables = snackbarHostMeasurables.fastMap { it.measure(looseConstraints) }

        val fabPlaceables = floatingActionButton.fastMapNotNull { measurable ->
            // respect only bottom and horizontal for snackbar and fab
            val leftInset = contentWindowInsets.getLeft(this, layoutDirection)
            val rightInset =
                contentWindowInsets.getRight(this, layoutDirection)
            val bottomInset = contentWindowInsets.getBottom(this)
            measurable
                .measure(looseConstraints.offset(-leftInset - rightInset, -bottomInset))
                .takeIf { it.height != 0 && it.width != 0 }
        }

        val fabPlacement = if (fabPlaceables.isNotEmpty()) {
            val fabWidth = fabPlaceables.fastMaxBy { it.width }!!.width
            val fabHeight = fabPlaceables.fastMaxBy { it.height }!!.height
            // FAB distance from the left of the layout, taking into account LTR / RTL
            val fabLeftOffset =
                when (floatingActionButtonPosition) {
                    FabPosition.Companion.Start -> {
                        if (layoutDirection == LayoutDirection.Ltr) {
                            FabSpacing.roundToPx()
                        } else {
                            layoutWidth - FabSpacing.roundToPx() - fabWidth
                        }
                    }
                    FabPosition.Companion.End,
                    FabPosition.Companion.EndOverlay -> {
                        if (layoutDirection == LayoutDirection.Ltr) {
                            layoutWidth - FabSpacing.roundToPx() - fabWidth
                        } else {
                            FabSpacing.roundToPx()
                        }
                    }
                    else -> (layoutWidth - fabWidth) / 2
                }

            FabPlacement(left = fabLeftOffset, width = fabWidth, height = fabHeight)
        } else {
            null
        }

        val fabOffsetFromBottom = fabPlacement?.let {
            if (it.height != 0) {
                it.height + FabSpacing.roundToPx() + contentWindowInsets.getBottom(this)
            } else {
                0
            }
        } ?: 0

        layout(layoutWidth, layoutHeight) {
            val sheetWidth = sheetPlaceables.fastMaxOfOrNull { it.width } ?: 0
            val sheetOffsetX = max(0, (layoutWidth - sheetWidth) / 2)

            val snackbarWidth = snackbarPlaceables.fastMaxOfOrNull { it.width } ?: 0
            val snackbarHeight = snackbarPlaceables.fastMaxOfOrNull { it.height } ?: 0
            val snackbarOffsetX = (layoutWidth - snackbarWidth) / 2
            val snackbarOffsetY =
                when (sheetState.currentValue) {
                    PartiallyExpanded -> sheetOffset().roundToInt() - snackbarHeight
                    Expanded,
                    Hidden,
                        -> layoutHeight - snackbarHeight
                }

            // Placement order is important for elevation
            bodyPlaceables.fastForEach { it.placeRelative(0, topBarHeight) }
            topBarPlaceables.fastForEach { it.placeRelative(0, 0) }
            fabPlacement?.let { placement ->
                fabPlaceables.fastForEach {
                    it.place(placement.left, layoutHeight - fabOffsetFromBottom)
                }
            }
            sheetPlaceables.fastForEach { it.placeRelative(sheetOffsetX, 0) }
            snackbarPlaceables.fastForEach { it.placeRelative(snackbarOffsetX, snackbarOffsetY) }
        }
    }
}

/**
 * Placement information for a [FloatingActionButton] inside a [Scaffold].
 *
 * @property left the FAB's offset from the left edge of the bottom bar, already adjusted for RTL
 * support
 * @property width the width of the FAB
 * @property height the height of the FAB
 */
@Immutable
internal class FabPlacement(
    val left: Int,
    val width: Int,
    val height: Int
)

private enum class BottomSheetScaffoldLayoutSlot { TopBar, Body, Sheet, Snackbar, Fab }

// FAB spacing above the bottom bar / bottom of the Scaffold
private val FabSpacing = 16.dp