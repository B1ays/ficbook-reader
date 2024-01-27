package ru.blays.ficbookReader.components.userProfile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import ru.blays.ficbookReader.shared.ui.profileComponents.declaration.UserProfileRootComponent
import ru.blays.ficbookReader.utils.LocalStackAnimator

@Composable
fun UserProfileRootContent(component: UserProfileRootComponent) {
    val animator = LocalStackAnimator.current
    Children(
        stack = component.childStack,
        modifier = Modifier.fillMaxSize(),
        animation = animator?.let { stackAnimation(it) }
    ) {
        when(
            val child = it.instance
        ) {
            is UserProfileRootComponent.Child.Profile -> UserProfileContent(child.component)
            is UserProfileRootComponent.Child.AddAccount -> LogInContent(child.component)
            is UserProfileRootComponent.Child.AccountManaging -> AccountsManagingContent(child.component)
        }
    }
}