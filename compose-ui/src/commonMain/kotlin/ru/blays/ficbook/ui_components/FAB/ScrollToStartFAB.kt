package ru.blays.ficbook.ui_components.FAB

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ficbook_reader.compose_ui.generated.resources.Res
import ficbook_reader.compose_ui.generated.resources.content_description_icon_up
import ficbook_reader.compose_ui.generated.resources.ic_arrow_up
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.utils.ScrollDirection
import ru.blays.ficbook.utils.rememberDirectionalLazyListState

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ScrollToStartFAB(lazyListState: LazyListState) {
    val scrollDirectionProvider = rememberDirectionalLazyListState(lazyListState)
    val scrollDirection = scrollDirectionProvider.scrollDirection
    var showFAB by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(scrollDirection) {
        when(scrollDirection) {
            ScrollDirection.Up -> {
                showFAB = true
            }
            ScrollDirection.Down -> {
                showFAB = false
            }
            else -> {}
        }
    }

    LaunchedEffect(
        lazyListState.canScrollBackward,
        lazyListState.canScrollForward
    ) {
        if(!lazyListState.canScrollBackward) {
            showFAB = false
        }
        if(!lazyListState.canScrollForward) {
            showFAB = true
        }
    }


    AnimatedVisibility(
        visible = showFAB,
        enter = fadeIn(initialAlpha = 1f),
        exit = fadeOut(targetAlpha = 0f),
    ) {
        FloatingActionButton(
            onClick = {
                scope.launch {
                    lazyListState.animateScrollToItem(0)
                    showFAB = false
                }
            },
            shape = CircleShape,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_up),
                contentDescription = stringResource(Res.string.content_description_icon_up),
                modifier = Modifier.size(32.dp),
            )
        }
    }
}