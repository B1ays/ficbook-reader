@file:Suppress("AnimateAsStateLabel")
@file:JvmName("ThemeCommonKt")

package ru.blays.ficbook.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.RippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import ru.blays.ficbook.reader.shared.components.themeComponents.ThemeComponent

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

fun createRippleConfig(
    color: Color,
    darkTheme: Boolean
): RippleConfiguration {
    return RippleConfiguration(
        color = color,
        rippleAlpha = if(darkTheme) {
            DarkThemeRippleAlpha
        } else {
            if (color.luminance() > 0.5) {
                LightThemeHighContrastRippleAlpha
            } else {
                LightThemeLowContrastRippleAlpha
            }
        }
    )
}

private val LightThemeHighContrastRippleAlpha = RippleAlpha(
    pressedAlpha = 0.64f,
    focusedAlpha = 0.64f,
    draggedAlpha = 0.56f,
    hoveredAlpha = 0.48f
)

private val LightThemeLowContrastRippleAlpha = RippleAlpha(
    pressedAlpha = 0.52f,
    focusedAlpha = 0.52f,
    draggedAlpha = 0.48f,
    hoveredAlpha = 0.44f
)

private val DarkThemeRippleAlpha = RippleAlpha(
    pressedAlpha = 0.60f,
    focusedAlpha = 0.62f,
    draggedAlpha = 0.58f,
    hoveredAlpha = 0.54f
)

@Composable
internal fun Color.animate(animationSpec: AnimationSpec<Color>): Color {
    return animateColorAsState(this, animationSpec).value
}