package ru.blays.ficbook.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.*
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
        primary = colorScheme.primary.animate(animationSpec),
        onPrimary = colorScheme.onPrimary.animate(animationSpec),
        primaryContainer = colorScheme.primaryContainer.animate(animationSpec),
        onPrimaryContainer = colorScheme.onPrimaryContainer.animate(animationSpec),
        inversePrimary = colorScheme.inversePrimary.animate(animationSpec),
        secondary = colorScheme.secondary.animate(animationSpec),
        onSecondary = colorScheme.onSecondary.animate(animationSpec),
        secondaryContainer = colorScheme.secondaryContainer.animate(animationSpec),
        onSecondaryContainer = colorScheme.onSecondaryContainer.animate(animationSpec),
        tertiary = colorScheme.tertiary.animate(animationSpec),
        onTertiary = colorScheme.onTertiary.animate(animationSpec),
        tertiaryContainer = colorScheme.tertiaryContainer.animate(animationSpec),
        onTertiaryContainer = colorScheme.onTertiaryContainer.animate(animationSpec),
        background = colorScheme.background.animate(animationSpec),
        onBackground = colorScheme.onBackground.animate(animationSpec),
        surface = colorScheme.surface.animate(animationSpec),
        onSurface = colorScheme.onSurface.animate(animationSpec),
        surfaceVariant = colorScheme.surfaceVariant.animate(animationSpec),
        onSurfaceVariant = colorScheme.onSurfaceVariant.animate(animationSpec),
        surfaceTint = colorScheme.surfaceTint.animate(animationSpec),
        inverseSurface = colorScheme.inverseSurface.animate(animationSpec),
        inverseOnSurface = colorScheme.inverseOnSurface.animate(animationSpec),
        error = colorScheme.error.animate(animationSpec),
        onError = colorScheme.onError.animate(animationSpec),
        errorContainer = colorScheme.errorContainer.animate(animationSpec),
        onErrorContainer = colorScheme.onErrorContainer.animate(animationSpec),
        outline = colorScheme.outline.animate(animationSpec),
        outlineVariant = colorScheme.outlineVariant.animate(animationSpec),
        scrim = colorScheme.scrim.animate(animationSpec),
        surfaceBright = colorScheme.surfaceBright.animate(animationSpec),
        surfaceDim = colorScheme.surfaceDim.animate(animationSpec),
        surfaceContainer = colorScheme.surfaceContainer.animate(animationSpec),
        surfaceContainerHigh = colorScheme.surfaceContainerHigh.animate(animationSpec),
        surfaceContainerHighest = colorScheme.surfaceContainerHighest.animate(animationSpec),
        surfaceContainerLow = colorScheme.surfaceContainerLow.animate(animationSpec),
        surfaceContainerLowest = colorScheme.surfaceContainerLowest.animate(animationSpec)
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
                LocalDynamicMaterialThemeSeed provides animatedColorScheme.primary,
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
    val seedColor = LocalDynamicMaterialThemeSeed.current

    val colorScheme = dynamicColorScheme(
        primary = seedColor,
        isAmoled = false,
        isDark = darkTheme,
    ) {
        if(darkTheme) {
            it.copy(
                background = darkColor,
                onBackground = lightColor
            )
        } else {
            it.copy(
                background = lightColor,
                onBackground = darkColor
            )
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(darkTheme) {
            val window = (view.context as Activity).window
            val windowsInsetsController = WindowCompat.getInsetsController(window, view)

            val oldColor = window.statusBarColor
            window.statusBarColor = colorScheme.surface.toArgb()

            val oldContrastEnforced: Boolean
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                oldContrastEnforced = window.isStatusBarContrastEnforced
                window.isNavigationBarContrastEnforced = false
                window.isStatusBarContrastEnforced = false
            } else {
                oldContrastEnforced = false
            }

            val oldAppearance = windowsInsetsController.isAppearanceLightStatusBars
            windowsInsetsController.isAppearanceLightStatusBars = !darkTheme
            windowsInsetsController.isAppearanceLightNavigationBars = !darkTheme

            onDispose {
                window.statusBarColor = oldColor

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = oldContrastEnforced
                    window.isStatusBarContrastEnforced = oldContrastEnforced
                }

                windowsInsetsController.isAppearanceLightStatusBars = oldAppearance
                windowsInsetsController.isAppearanceLightNavigationBars = oldAppearance
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