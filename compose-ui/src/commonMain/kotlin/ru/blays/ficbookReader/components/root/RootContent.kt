package ru.blays.ficbookReader.components.root

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import ru.blays.ficbookReader.components.fanficPage.FanficPageContent
import ru.blays.ficbookReader.components.fanficsList.FanficsListScreenContent
import ru.blays.ficbookReader.components.main.MainContent
import ru.blays.ficbookReader.components.userProfile.LoginContent
import ru.blays.ficbookReader.shared.ui.RootComponent.RootComponent

@Composable
fun RootContent(component: RootComponent) {
    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        Children(
            stack = component.childStack,
            animation = stackAnimation(fade() + scale())
        ) {
            when(val child = it.instance) {
                is RootComponent.Child.FanficPage -> FanficPageContent(child.component)
                is RootComponent.Child.Login -> LoginContent(child.component)
                is RootComponent.Child.Main -> MainContent(child.component)
                is RootComponent.Child.Settings -> TODO()
                is RootComponent.Child.FanficsList -> FanficsListScreenContent(child.component)
            }
        }
    }
}