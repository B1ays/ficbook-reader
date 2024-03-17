@file:Suppress("AnimateAsStateLabel")

package ru.blays.ficbook.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.materialkolor.LocalDynamicMaterialThemeSeed
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme
import ru.blays.ficbook.reader.shared.components.themeComponents.ThemeComponent

@Composable
actual fun AppTheme(
    component: ThemeComponent,
    content: @Composable () -> Unit
) {
    val state by component.state.subscribeAsState()
    val themeIndex = state.themeIndex
    val isAmoledTheme = state.amoledTheme
    val colorAccentIndex = state.defaultAccentIndex

    val primaryColor = remember(colorAccentIndex) {
        defaultAccentColors.getOrElse(colorAccentIndex) {
            defaultAccentColors.first()
        }
    }

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val darkTheme = remember(key1 = themeIndex) {
        when(themeIndex) {
            0 -> isSystemInDarkTheme
            1 -> true
            2 -> false
            else -> isSystemInDarkTheme
        }
    }

    val animationSpec: AnimationSpec<Color> = remember {
        spring(stiffness = Spring.StiffnessLow)
    }

    val colorScheme: ColorScheme = rememberDynamicColorScheme(
        seedColor = primaryColor,
        isDark = darkTheme,
        style = PaletteStyle.TonalSpot
    ).run {
        if(isAmoledTheme && darkTheme) copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceVariant = Color.Black,
            surfaceContainer = Color.Black,
            surfaceTint = Color.Black
        ) else this
    }

    val animatedColorScheme = colorScheme.copy(
        primary = animateColorAsState(colorScheme.primary, animationSpec).value,
        primaryContainer = animateColorAsState(colorScheme.primaryContainer, animationSpec).value,
        secondary = animateColorAsState(colorScheme.secondary, animationSpec).value,
        secondaryContainer = animateColorAsState(colorScheme.secondaryContainer, animationSpec).value,
        tertiary = animateColorAsState(colorScheme.tertiary, animationSpec).value,
        tertiaryContainer = animateColorAsState(colorScheme.tertiaryContainer, animationSpec).value,
        background = animateColorAsState(colorScheme.background, animationSpec).value,
        surface = animateColorAsState(colorScheme.surface, animationSpec).value,
        error = animateColorAsState(colorScheme.error, animationSpec).value,
        onPrimary = animateColorAsState(colorScheme.onPrimary, animationSpec).value,
        onSecondary = animateColorAsState(colorScheme.onSecondary, animationSpec).value,
        onTertiary = animateColorAsState(colorScheme.onTertiary, animationSpec).value,
        onBackground = animateColorAsState(colorScheme.onBackground, animationSpec).value,
        onSurface = animateColorAsState(colorScheme.onSurface, animationSpec).value,
        onError = animateColorAsState(colorScheme.onError, animationSpec).value,
        onPrimaryContainer = animateColorAsState(colorScheme.onPrimaryContainer, animationSpec).value,
        inversePrimary = animateColorAsState(colorScheme.inversePrimary, animationSpec).value,
        onSecondaryContainer = animateColorAsState(colorScheme.onSecondaryContainer, animationSpec).value,
        onTertiaryContainer = animateColorAsState(colorScheme.onTertiaryContainer, animationSpec).value,
        surfaceVariant = animateColorAsState(colorScheme.surfaceVariant, animationSpec).value,
        onSurfaceVariant = animateColorAsState(colorScheme.onSurfaceVariant, animationSpec).value,
        surfaceTint = animateColorAsState(colorScheme.surfaceTint, animationSpec).value,
        inverseSurface = animateColorAsState(colorScheme.inverseSurface, animationSpec).value,
        inverseOnSurface = animateColorAsState(colorScheme.inverseOnSurface, animationSpec).value,
        errorContainer = animateColorAsState(colorScheme.errorContainer, animationSpec).value,
        onErrorContainer = animateColorAsState(colorScheme.onErrorContainer, animationSpec).value,
        outline = animateColorAsState(colorScheme.outline, animationSpec).value,
        outlineVariant = animateColorAsState(colorScheme.outlineVariant, animationSpec).value,
        scrim = animateColorAsState(colorScheme.scrim, animationSpec).value
    )

    val rippleTheme = remember(animatedColorScheme) {
        PrimaryRippleTheme(
            primaryColor = animatedColorScheme.primary,
            isDarkTheme = { darkTheme }
        )
    }

    MaterialTheme(
        colorScheme = animatedColorScheme,
        content = {
            CompositionLocalProvider(
                LocalRippleTheme provides rippleTheme,
                LocalDynamicMaterialThemeSeed provides primaryColor,
                content = content
            )
        },
        typography = Typography
    )
}

@Composable
actual fun ReaderTheme(
    darkTheme: Boolean,
    darkColor: Color,
    lightColor: Color,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> darkColorScheme(
            background = darkColor,
            onBackground = lightColor
        )
        else -> lightColorScheme(
            background = lightColor,
            onBackground = darkColor
        )
    }

    class CustomRippleTheme(private val rippleColor: Color) : RippleTheme {

        @Composable
        override fun defaultColor(): Color =
            RippleTheme.defaultRippleColor(
                rippleColor,
                lightTheme = !darkTheme
            )

        @Composable
        override fun rippleAlpha(): RippleAlpha =
            RippleTheme.defaultRippleAlpha(
                rippleColor.copy(alpha = 0.75f),
                lightTheme = !darkTheme
            )
    }
    CompositionLocalProvider(LocalRippleTheme provides CustomRippleTheme(colorScheme.primary)) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}