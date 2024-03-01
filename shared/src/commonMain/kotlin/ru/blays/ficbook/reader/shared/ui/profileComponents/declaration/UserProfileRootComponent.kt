package ru.blays.ficbook.reader.shared.ui.profileComponents.declaration

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable

interface UserProfileRootComponent {
    val childStack: Value<ChildStack<*, Child>>

    fun onOutput(output: Output)

    sealed class Child {
        data class AddAccount(val component: UserLogInComponent): Child()
        data class Profile(val component: UserProfileComponent): Child()
        data class AccountManaging(val component: UserProfileManagingComponent): Child()
    }

    @Serializable
    sealed class Config {
        @Serializable
        data object LogIn: Config()
        @Serializable
        data object Profile: Config()
        @Serializable
        data object AccountManaging: Config()
    }

    sealed class Output {
        data object NavigateBack: Output()
        data object OpenMainScreen : Output()
        data class OpenProfile(val userHref: String): Output()
    }
}