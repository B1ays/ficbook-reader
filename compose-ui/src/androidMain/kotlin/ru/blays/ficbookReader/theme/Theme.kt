@file:Suppress("AnimateAsStateLabel")

package ru.blays.ficbookReader.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.materialkolor.LocalDynamicMaterialThemeSeed
import com.materialkolor.dynamicColorScheme
import ru.blays.ficbookReader.shared.ui.themeComponents.ThemeComponent

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
    val darkTheme = remember(key1 = themeIndex) {
        when(themeIndex) {
            0 -> isSystemInDarkTheme
            1 -> true
            2 -> false
            else -> isSystemInDarkTheme
        }
    }


    val primaryColor = remember(colorAccentIndex) {
        defaultAccentColorsList.getOrElse(colorAccentIndex) {
            defaultAccentColorsList.first()
        }
    }

    val animationSpec: AnimationSpec<Color> = remember {
        spring(stiffness = Spring.StiffnessLow)
    }

    val colors: ColorScheme by remember(primaryColor, darkTheme, isAmoledTheme, monetTheme) {
        derivedStateOf {
            val scheme = when {
                monetTheme && darkTheme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicDarkColorScheme(context)
                monetTheme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
                else -> dynamicColorScheme(
                    seedColor = primaryColor,
                    isDark = darkTheme
                )
            }
            scheme.run {
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
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
    }

    CompositionLocalProvider(LocalDynamicMaterialThemeSeed provides primaryColor) {
        MaterialTheme(
            colorScheme = animatedColorScheme,
            content = content,
            typography = Typography
        )
    }
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