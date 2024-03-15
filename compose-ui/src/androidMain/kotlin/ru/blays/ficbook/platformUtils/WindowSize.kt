package ru.blays.ficbook.platformUtils

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntSize

@SuppressLint("ComposableNaming")
@Composable
actual fun WindowSize(): IntSize {
    return with(LocalConfiguration.current) { IntSize(screenWidthDp, screenHeightDp) }
}

actual val landscapeModeWidth: Int = 700

actual const val scaleContent: Boolean = false

@Composable
actual fun Modifier.landscapeInsetsPadding(): Modifier {
    val orientation = LocalConfiguration.current

    return if (orientation.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        this then Modifier.systemBarsPadding()
    } else {
        this
    }
}