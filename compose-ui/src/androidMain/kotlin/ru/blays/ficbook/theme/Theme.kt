@file:Suppress("AnimateAsStateLabel")

package ru.blays.ficbook.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
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

    val colors: ColorScheme by remember(
        primaryColor,
        darkTheme,
        isAmoledTheme,
        monetTheme
    ) {
        derivedStateOf {
            val scheme = when {
                monetTheme && darkTheme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicDarkColorScheme(context)
                monetTheme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
                else -> dynamicColorScheme(
                    seedColor = primaryColor,
                    isDark = darkTheme,
                    isAmoled = false,
                    style = PaletteStyle.Content
                )
            }
            scheme.let {
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

    val animatedColorScheme = colors.copy(
        primary = colors.primary.animate(animationSpec),
        primaryContainer = colors.primaryContainer.animate(animationSpec),
        secondary = colors.secondary.animate(animationSpec),
        secondaryContainer = colors.secondaryContainer.animate(animationSpec),
        tertiary = colors.tertiary.animate(animationSpec),
        tertiaryContainer = colors.tertiaryContainer.animate(animationSpec),
        background = colors.background.animate(animationSpec),
        surface = colors.surface.animate(animationSpec),
        surfaceTint = colors.surfaceTint.animate(animationSpec),
        surfaceBright = colors.surfaceBright.animate(animationSpec),
        surfaceDim = colors.surfaceDim.animate(animationSpec),
        surfaceContainer = colors.surfaceContainer.animate(animationSpec),
        surfaceContainerHigh = colors.surfaceContainerHigh.animate(animationSpec),
        surfaceContainerHighest = colors.surfaceContainerHighest.animate(animationSpec),
        surfaceContainerLow = colors.surfaceContainerLow.animate(animationSpec),
        surfaceContainerLowest = colors.surfaceContainerLowest.animate(animationSpec),
        surfaceVariant = colors.surfaceVariant.animate(animationSpec),
        error = colors.error.animate(animationSpec),
        errorContainer = colors.errorContainer.animate(animationSpec),
        onPrimary = colors.onPrimary.animate(animationSpec),
        onPrimaryContainer = colors.onPrimaryContainer.animate(animationSpec),
        onSecondary = colors.onSecondary.animate(animationSpec),
        onSecondaryContainer = colors.onSecondaryContainer.animate(animationSpec),
        onTertiary = colors.onTertiary.animate(animationSpec),
        onTertiaryContainer = colors.onTertiaryContainer.animate(animationSpec),
        onBackground = colors.onBackground.animate(animationSpec),
        onSurface = colors.onSurface.animate(animationSpec),
        onSurfaceVariant = colors.onSurfaceVariant.animate(animationSpec),
        onError = colors.onError.animate(animationSpec),
        onErrorContainer = colors.onErrorContainer.animate(animationSpec),
        inversePrimary = colors.inversePrimary.animate(animationSpec),
        inverseSurface = colors.inverseSurface.animate(animationSpec),
        inverseOnSurface = colors.inverseOnSurface.animate(animationSpec),
        outline = colors.outline.animate(animationSpec),
        outlineVariant = colors.outlineVariant.animate(animationSpec),
        scrim = colors.scrim.animate(animationSpec),
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
    val parentColorScheme = MaterialTheme.colorScheme

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