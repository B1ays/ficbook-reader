package ru.blays.ficbook.platformUtils

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
fun FullscreenContainer(
    enabled: Boolean,
    content: @Composable () -> Unit
) {
    // Make app fullscreen
    val view = LocalView.current
    val window = (view.context as Activity).window

    DisposableEffect(key1 = enabled) {
        val windowInsetsController =
            WindowCompat.getInsetsController(window, view)
        val systemBarColor = window.statusBarColor
        val navigationBarColor = window.navigationBarColor
        val fitsSystemWindows = window.decorView.fitsSystemWindows

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
            window.decorView.fitsSystemWindows = fitsSystemWindows
            window.statusBarColor = systemBarColor
            window.navigationBarColor = navigationBarColor
        }
    }
    content()
}