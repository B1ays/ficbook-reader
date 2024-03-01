package ru.blays.ficbook.platformUtils

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntSize

@SuppressLint("ComposableNaming")
@Composable
actual fun WindowSize(): IntSize {
    return with(LocalConfiguration.current) { IntSize(screenWidthDp, screenHeightDp) }
}

actual val landscapeModeWidth: Int = 700