package ru.blays.ficbookReader.platformUtils

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
actual fun FullscreenContainer(enabled: Boolean, content: @Composable () -> Unit) {
    // Make app fullscreen
    val view = LocalView.current
    val window = remember { (view.context as Activity).window }

    DisposableEffect(key1 = enabled) {
        val windowInsetsController =
            WindowCompat.getInsetsController(window, view)

        if (!view.isInEditMode && enabled) {
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            windowInsetsController.hide(
                WindowInsetsCompat.Type.systemBars()
            )
        }
        onDispose {
            windowInsetsController.show(
                WindowInsetsCompat.Type.systemBars()
            )
        }
    }
    content()
}