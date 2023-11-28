package ru.blays.ficbookReader.components.root

import androidx.compose.animation.core.spring
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import ru.blays.ficbookReader.components.authorProfile.AuthorProfileContent
import ru.blays.ficbookReader.components.fanficPage.FanficPageContent
import ru.blays.ficbookReader.components.fanficsList.FanficsListScreenContent
import ru.blays.ficbookReader.components.main.MainContent
import ru.blays.ficbookReader.components.settings.SettingsContent
import ru.blays.ficbookReader.components.userProfile.UserProfileRootContent
import ru.blays.ficbookReader.shared.ui.RootComponent.RootComponent
import ru.blays.ficbookReader.utils.LocalStackAnimator

@Composable
fun RootContent(component: RootComponent) {
    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        val animator = fade(spring()) + scale()
        CompositionLocalProvider(
            LocalStackAnimator provides fade(spring()) + scale()
        ) {
            Children(
                stack = component.childStack,
                animation = stackAnimation(animator)
            ) {
                when(
                    val child = it.instance
                ) {
                    is RootComponent.Child.FanficPage -> FanficPageContent(child.component)
                    is RootComponent.Child.UserProfile -> UserProfileRootContent(child.component)
                    is RootComponent.Child.Main -> MainContent(child.component)
                    is RootComponent.Child.Settings -> SettingsContent(child.component)
                    is RootComponent.Child.FanficsList -> FanficsListScreenContent(child.component)
                    is RootComponent.Child.AuthorProfile -> AuthorProfileContent(child.component)
                }
            }
        }
    }
}