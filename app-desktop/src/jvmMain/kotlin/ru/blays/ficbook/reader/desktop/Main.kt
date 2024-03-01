package ru.blays.ficbook.reader.desktop

import androidx.compose.material.ButtonDefaults
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import org.koin.core.context.startKoin
import ru.blays.ficbook.reader.feature.fileDownload.downloadFeatureModule
import ru.blays.ficbook.components.root.RootContent
import ru.blays.ficbook.platformUtils.createImageLoader
import ru.blays.ficbook.reader.shared.di.sharedModule
import ru.blays.ficbook.reader.shared.ui.RootComponent.DefaultRootComponent
import ru.blays.ficbook.theme.AppTheme

@OptIn(ExperimentalDecomposeApi::class, ExperimentalCoilApi::class)
fun main() {
    startKoin {
        modules(
            sharedModule,
            downloadFeatureModule
        )
    }

    val lifecycle = LifecycleRegistry()

    val root = runOnUiThread {
        DefaultRootComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
        )
    }

    application {
        setSingletonImageLoaderFactory { context ->
            createImageLoader(context)
        }

        val windowState = rememberWindowState(
            isMinimized = false
        )

        LifecycleController(lifecycle, windowState)
        ButtonDefaults.buttonColors()
        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "Ficbook reader",
            icon = painterResource("icon_windows.ico")
        ) {
            AppTheme(root.themeComponent) {
                RootContent(root)
            }
        }
    }
}