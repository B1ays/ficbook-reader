package ru.blays.ficbookReader.android

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.retainedComponent
import ru.blays.ficbookReader.components.root.RootContent
import ru.blays.ficbookReader.shared.ui.RootComponent.DefaultRootComponent
import ru.blays.ficbookReader.theme.AppTheme

@OptIn(ExperimentalDecomposeApi::class)
class MainActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deepLinkData: Uri? = intent?.data

        val root = retainedComponent { componentContext ->
            DefaultRootComponent(
                componentContext = componentContext,
                deepLink = deepLinkData.toString()
            )
        }

        setContent {
            AppTheme(root.themeComponent) {
                RootContent(component = root)
            }
        }
    }
}
