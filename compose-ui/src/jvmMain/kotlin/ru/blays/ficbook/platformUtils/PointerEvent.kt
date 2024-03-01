package ru.blays.ficbook.platformUtils

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.*

@ExperimentalComposeUiApi
actual fun Modifier.onPointerEventPlatform(
    eventType: PointerEventType,
    pass: PointerEventPass,
    onEvent: AwaitPointerEventScope.(event: PointerEvent) -> Unit
): Modifier = onPointerEvent(
    eventType = eventType,
    pass = pass,
    onEvent = onEvent

)