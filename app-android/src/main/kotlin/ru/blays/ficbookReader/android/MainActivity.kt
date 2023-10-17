package ru.blays.ficbookReader.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arkivanov.decompose.defaultComponentContext
import ru.blays.ficbookReader.components.root.RootContent
import ru.blays.ficbookReader.shared.ui.RootComponent.DefaultRootComponent
import ru.blays.ficbookReader.theme.AppTheme

class MainActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        println("Start activity")

        val root = DefaultRootComponent(componentContext = defaultComponentContext())

        setContent {
            AppTheme {
                RootContent(component = root)
            }
        }
    }
}
