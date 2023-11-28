package ru.blays.ficbookReader.shared.ui.profileComponents

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable

interface UserProfileRootComponent {
    val childStack: Value<ChildStack<*, Child>>

    fun onOutput(output: Output)

    sealed class Child {
        data class LogIn(val component: UserLogInComponent): Child()
        data class Profile(val component: UserProfileComponent): Child()
    }

    @Serializable
    sealed class Config {
        @Serializable
        data object LogIn: Config()
        @Serializable
        data object Profile: Config()
    }

    sealed class Output {
        data object NavigateBack: Output()
        data class OpenProfile(val userHref: String): Output()
    }
}