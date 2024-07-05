@file:Suppress("AnimateAsStateLabel")

package ru.blays.ficbook.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
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

    val animatedColorScheme = ColorScheme(
        primary = animateColorAsState(colorScheme.primary, animationSpec).value,
        onPrimary = animateColorAsState(colorScheme.onPrimary, animationSpec).value,
        primaryContainer = animateColorAsState(colorScheme.primaryContainer, animationSpec).value,
        onPrimaryContainer = animateColorAsState(colorScheme.onPrimaryContainer, animationSpec).value,
        inversePrimary = animateColorAsState(colorScheme.inversePrimary, animationSpec).value,
        secondary = animateColorAsState(colorScheme.secondary, animationSpec).value,
        onSecondary = animateColorAsState(colorScheme.onSecondary, animationSpec).value,
        secondaryContainer = animateColorAsState(colorScheme.secondaryContainer, animationSpec).value,
        onSecondaryContainer = animateColorAsState(colorScheme.onSecondaryContainer, animationSpec).value,
        tertiary = animateColorAsState(colorScheme.tertiary, animationSpec).value,
        onTertiary = animateColorAsState(colorScheme.onTertiary, animationSpec).value,
        tertiaryContainer = animateColorAsState(colorScheme.tertiaryContainer, animationSpec).value,
        onTertiaryContainer = animateColorAsState(colorScheme.onTertiaryContainer, animationSpec).value,
        background = animateColorAsState(colorScheme.background, animationSpec).value,
        onBackground = animateColorAsState(colorScheme.onBackground, animationSpec).value,
        surface = animateColorAsState(colorScheme.surface, animationSpec).value,
        onSurface = animateColorAsState(colorScheme.onSurface, animationSpec).value,
        surfaceVariant = animateColorAsState(colorScheme.surfaceVariant, animationSpec).value,
        onSurfaceVariant = animateColorAsState(colorScheme.onSurfaceVariant, animationSpec).value,
        surfaceTint = animateColorAsState(colorScheme.surfaceTint, animationSpec).value,
        inverseSurface = animateColorAsState(colorScheme.inverseSurface, animationSpec).value,
        inverseOnSurface = animateColorAsState(colorScheme.inverseOnSurface, animationSpec).value,
        error = animateColorAsState(colorScheme.error, animationSpec).value,
        onError = animateColorAsState(colorScheme.onError, animationSpec).value,
        errorContainer = animateColorAsState(colorScheme.errorContainer, animationSpec).value,
        onErrorContainer = animateColorAsState(colorScheme.onErrorContainer, animationSpec).value,
        outline = animateColorAsState(colorScheme.outline, animationSpec).value,
        outlineVariant = animateColorAsState(colorScheme.outlineVariant, animationSpec).value,
        scrim = animateColorAsState(colorScheme.scrim, animationSpec).value,
        surfaceBright = animateColorAsState(colorScheme.surfaceBright, animationSpec).value,
        surfaceDim = animateColorAsState(colorScheme.surfaceDim, animationSpec).value,
        surfaceContainer = animateColorAsState(colorScheme.surfaceContainer, animationSpec).value,
        surfaceContainerHigh = animateColorAsState(colorScheme.surfaceContainerHigh, animationSpec).value,
        surfaceContainerHighest = animateColorAsState(colorScheme.surfaceContainerHighest, animationSpec).value,
        surfaceContainerLow = animateColorAsState(colorScheme.surfaceContainerLow, animationSpec).value,
        surfaceContainerLowest = animateColorAsState(colorScheme.surfaceContainerLowest, animationSpec).value,
    )

    val rippleConfig = createRippleConfig(
        color = animatedColorScheme.primary,
        darkTheme = darkTheme
    )

    MaterialTheme(
        colorScheme = animatedColorScheme,
        content = {
            CompositionLocalProvider(
                LocalRippleConfiguration provides rippleConfig,
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

    val rippleConfig = createRippleConfig(
        color = colorScheme.primary,
        darkTheme = darkTheme
    )

    CompositionLocalProvider(
        LocalRippleConfiguration provides rippleConfig
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}