@file:Suppress("AnimateAsStateLabel")

package ru.blays.ficbookReader.theme

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
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.materialkolor.LocalDynamicMaterialThemeSeed
import com.materialkolor.dynamicColorScheme
import ru.blays.ficbookReader.shared.ui.themeComponents.ThemeComponent

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
        defaultAccentColorsList.getOrElse(colorAccentIndex) {
            defaultAccentColorsList.first()
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

    val colors: ColorScheme by remember(primaryColor, darkTheme, isAmoledTheme) {
        derivedStateOf {
            dynamicColorScheme(
                seedColor = primaryColor,
                isDark = darkTheme
            ).run {
                if(isAmoledTheme && darkTheme) copy(
                    background = Color.Black,
                    surface = Color.Black,
                    surfaceVariant = Color.Black,
                    //surfaceContainer = Color.Black,
                    surfaceTint = Color.Black
                ) else this
            }
        }
    }

    val animatedColorScheme = colors.copy(
        primary = animateColorAsState(colors.primary, animationSpec).value,
        primaryContainer = animateColorAsState(colors.primaryContainer, animationSpec).value,
        secondary = animateColorAsState(colors.secondary, animationSpec).value,
        secondaryContainer = animateColorAsState(colors.secondaryContainer, animationSpec).value,
        tertiary = animateColorAsState(colors.tertiary, animationSpec).value,
        tertiaryContainer = animateColorAsState(colors.tertiaryContainer, animationSpec).value,
        background = animateColorAsState(colors.background, animationSpec).value,
        surface = animateColorAsState(colors.surface, animationSpec).value,
        error = animateColorAsState(colors.error, animationSpec).value,
        onPrimary = animateColorAsState(colors.onPrimary, animationSpec).value,
        onSecondary = animateColorAsState(colors.onSecondary, animationSpec).value,
        onTertiary = animateColorAsState(colors.onTertiary, animationSpec).value,
        onBackground = animateColorAsState(colors.onBackground, animationSpec).value,
        onSurface = animateColorAsState(colors.onSurface, animationSpec).value,
        onError = animateColorAsState(colors.onError, animationSpec).value,
        onPrimaryContainer = animateColorAsState(colors.onPrimaryContainer, animationSpec).value,
        inversePrimary = animateColorAsState(colors.inversePrimary, animationSpec).value,
        onSecondaryContainer = animateColorAsState(colors.onSecondaryContainer, animationSpec).value,
        onTertiaryContainer = animateColorAsState(colors.onTertiaryContainer, animationSpec).value,
        surfaceVariant = animateColorAsState(colors.surfaceVariant, animationSpec).value,
        onSurfaceVariant = animateColorAsState(colors.onSurfaceVariant, animationSpec).value,
        surfaceTint = animateColorAsState(colors.surfaceTint, animationSpec).value,
        inverseSurface = animateColorAsState(colors.inverseSurface, animationSpec).value,
        inverseOnSurface = animateColorAsState(colors.inverseOnSurface, animationSpec).value,
        errorContainer = animateColorAsState(colors.errorContainer, animationSpec).value,
        onErrorContainer = animateColorAsState(colors.onErrorContainer, animationSpec).value,
        outline = animateColorAsState(colors.outline, animationSpec).value,
        outlineVariant = animateColorAsState(colors.outlineVariant, animationSpec).value,
        scrim = animateColorAsState(colors.scrim, animationSpec).value
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
                LocalDynamicMaterialThemeSeed provides primaryColor
            ) {
                content()
            }
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