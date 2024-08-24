package ru.blays.ficbook.components.userProfile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import ru.blays.ficbook.reader.shared.components.profileComponents.declaration.UserProfileRootComponent
import ru.blays.ficbook.utils.defaultStackAnimation

@Composable
fun UserProfileRootContent(component: UserProfileRootComponent) {
    Children(
        stack = component.childStack,
        modifier = Modifier.fillMaxSize(),
        animation = defaultStackAnimation()
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