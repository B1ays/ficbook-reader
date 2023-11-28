package ru.blays.ficbookReader.components.userProfile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import ru.blays.ficbookReader.shared.ui.profileComponents.UserProfileRootComponent

@Composable
fun UserProfileRootContent(component: UserProfileRootComponent) {
    Children(
        stack = component.childStack,
        modifier = Modifier.fillMaxSize(),
        animation = stackAnimation(fade())
    ) {
        when(
            val child = it.instance
        ) {
            is UserProfileRootComponent.Child.Profile -> UserProfileContent(child.component)
            is UserProfileRootComponent.Child.LogIn -> LogInContent(child.component)
        }
    }
}