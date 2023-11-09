package ru.blays.ficbookReader.android

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.retainedComponent
import ru.blays.ficbookReader.components.fanficPage.reader.LocalVolumeKeysEventSource
import ru.blays.ficbookReader.components.fanficPage.reader.TwoWayVolumeKeysEventAdapter
import ru.blays.ficbookReader.components.root.RootContent
import ru.blays.ficbookReader.shared.ui.RootComponent.DefaultRootComponent
import ru.blays.ficbookReader.theme.AppTheme

@OptIn(ExperimentalDecomposeApi::class)
class MainActivity: ComponentActivity() {
    private val volumeKeyEventSource =  TwoWayVolumeKeysEventAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deepLinkData: Uri? = intent?.data

        val root = retainedComponent { componentContext ->
            DefaultRootComponent(
                componentContext = componentContext,
                deepLink = deepLinkData.toString()
            )
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AppTheme(root.themeComponent) {
                CompositionLocalProvider(
                    LocalVolumeKeysEventSource provides volumeKeyEventSource
                ) {
                    RootContent(component = root)
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        val keyCode = event?.keyCode
        val actionKeyDown = event?.action == KeyEvent.ACTION_DOWN

        return when {
            keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && actionKeyDown -> {
                volumeKeyEventSource.onEvent(KeyEvent.KEYCODE_VOLUME_DOWN)
            }
            keyCode == KeyEvent.KEYCODE_VOLUME_UP && actionKeyDown -> {
                volumeKeyEventSource.onEvent(KeyEvent.KEYCODE_VOLUME_UP)
            }
            else -> super.dispatchKeyEvent(event)
        }
    }
}
