package ru.blays.ficbook.reader.android

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.retainedComponent
import ru.blays.ficbook.components.fanficPage.reader.LocalVolumeKeysEventSource
import ru.blays.ficbook.components.fanficPage.reader.TwoWayVolumeKeysEventAdapter
import ru.blays.ficbook.components.root.RootContent
import ru.blays.ficbook.reader.shared.ui.RootComponent.DefaultRootComponent
import ru.blays.ficbook.reader.shared.ui.RootComponent.RootComponent
import ru.blays.ficbook.theme.AppTheme

@OptIn(ExperimentalDecomposeApi::class)
class MainActivity: ComponentActivity() {
    private val volumeKeyEventSource = TwoWayVolumeKeysEventAdapter()
    private var rootComponent: RootComponent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermission(
                permission = Manifest.permission.POST_NOTIFICATIONS,
                requestCode = 1111
            )
        }

        val initialLink: String? = getLinkFromIntent(intent)

        rootComponent = retainedComponent { componentContext ->
            DefaultRootComponent(
                componentContext = componentContext,
                deepLink = initialLink
            )
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AppTheme(rootComponent!!.themeComponent) {
                CompositionLocalProvider(
                    LocalVolumeKeysEventSource provides volumeKeyEventSource
                ) {
                    RootContent(component = rootComponent!!)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        rootComponent = null
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val newLink: String = getLinkFromIntent(intent) ?: return
        rootComponent?.sendIntent(
            RootComponent.Intent.NewDeepLink(newLink)
        )
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

    // Function to check and request permission.
    private fun checkPermission(
        permission: String,
        requestCode: Int
    ) {
        if (ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
        }
    }

    private fun getLinkFromIntent(intent: Intent?): String? {
        return when(intent?.action) {
            Intent.ACTION_VIEW -> {
                intent.data.toString()
            }
            Intent.ACTION_SEND -> {
                intent.getStringExtra(Intent.EXTRA_TEXT)
            }
            else -> null
        }
    }
}
