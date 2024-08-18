@file:Suppress("AnimateAsStateLabel")

package ru.blays.ficbook.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.materialkolor.LocalDynamicMaterialThemeSeed
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import ru.blays.ficbook.reader.shared.components.themeComponents.ThemeComponent

@Composable
actual fun AppTheme(
    component: ThemeComponent,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val state by component.state.subscribeAsState()
    val themeIndex = state.themeIndex
    val isAmoledTheme = state.amoledTheme
    val colorAccentIndex = state.defaultAccentIndex
    val monetTheme = state.dynamicColors

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val darkTheme = when(themeIndex) {
        0 -> isSystemInDarkTheme
        1 -> true
        2 -> false
        else -> isSystemInDarkTheme
    }


    val primaryColor = remember(colorAccentIndex) {
        defaultAccentColors.getOrElse(colorAccentIndex) {
            defaultAccentColors.first()
        }
    }

    val animationSpec: AnimationSpec<Color> = spring(stiffness = 300F, dampingRatio = .6F)

    val colorScheme: ColorScheme by remember(
        primaryColor,
        darkTheme,
        isAmoledTheme,
        monetTheme
    ) {
        derivedStateOf {
            when {
                monetTheme && darkTheme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicDarkColorScheme(context)
                monetTheme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
                else -> dynamicColorScheme(
                    seedColor = primaryColor,
                    isDark = darkTheme,
                    isAmoled = false,
                    style = PaletteStyle.Content
                )
            }.let {
                if(isAmoledTheme && darkTheme) it.copy(
                    background = Color.Black,
                    surface = Color.Black,
                    surfaceVariant = Color.Black,
                    surfaceContainer = Color.Black,
                    surfaceTint = Color.Black
                ) else it
            }
        }
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

    val view = LocalView.current

    LaunchedEffect(darkTheme) {
        val window = (view.context as Activity).window

        val transparentColor = Color.Transparent.toArgb()
        window.statusBarColor = transparentColor
        window.navigationBarColor = transparentColor

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        val windowsInsetsController = WindowCompat.getInsetsController(window, view)
        windowsInsetsController.isAppearanceLightStatusBars = !darkTheme
        windowsInsetsController.isAppearanceLightNavigationBars = !darkTheme
    }

    val rippleConfiguration = createRippleConfig(animatedColorScheme.primary, darkTheme)

    MaterialTheme(
        colorScheme = animatedColorScheme,
        content = {
            CompositionLocalProvider(
                LocalRippleConfiguration provides rippleConfiguration,
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
    val parentColorScheme = colorScheme

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

    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(key1 = Unit) {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()

            onDispose {
                window.statusBarColor = parentColorScheme.background.toArgb()
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

actual fun createRippleConfig(
    color: Color,
    darkTheme: Boolean,
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