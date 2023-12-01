@file:Suppress("AnimateAsStateLabel")

package ru.blays.ficbookReader.theme

import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ru.blays.ficbookReader.shared.ui.themeComponents.ThemeComponent

@Composable
expect fun AppTheme(
    component: ThemeComponent,
    content: @Composable () -> Unit
)

@Composable
expect fun ReaderTheme(
    darkTheme: Boolean,
    darkColor: Color,
    lightColor: Color,
    content: @Composable () -> Unit
)

class PrimaryRippleTheme(
    private val primaryColor: Color,
    private val isDarkTheme: () -> Boolean
): RippleTheme {
    @Composable
    override fun defaultColor() = primaryColor

    @Composable
    override fun rippleAlpha() = RippleTheme.defaultRippleAlpha(
        primaryColor,
        isDarkTheme()
    )
}