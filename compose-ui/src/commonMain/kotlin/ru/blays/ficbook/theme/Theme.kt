@file:Suppress("AnimateAsStateLabel")
@file:JvmName("ThemeCommonKt")

package ru.blays.ficbook.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.RippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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

expect fun createRippleConfig(
    color: Color,
    darkTheme: Boolean
): RippleConfiguration

internal fun RippleConfiguration.copy(
    color: Color = this.color,
    rippleAlpha: RippleAlpha? = this.rippleAlpha
) = RippleConfiguration(
    color = color,
    rippleAlpha = rippleAlpha
)

@Composable
internal fun Color.animate(animationSpec: AnimationSpec<Color>): Color {
    return animateColorAsState(this, animationSpec).value
}