@file:OptIn(ExperimentalComposeUiApi::class)

package ru.blays.ficbookReader.platformUtils

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntSize

@Composable
actual fun WindowSize(): IntSize {
    return LocalWindowInfo.current.containerSize
}

actual val landscapeModeWidth: Int = 600