package ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration

import com.arkivanov.decompose.value.Value
import ru.blays.ficbookapi.dataModels.CookieModel

interface UserLogInComponent {
    val state: Value<State>

    fun sendIntent(intent: Intent)
    fun onOutput(output: Output)

    sealed class Intent {
        data class LoginChanged(val login: String): Intent()
        data class PasswordChanged(val password: String): Intent()
        data object LogIn: Intent()
        data object LogOut: Intent()
    }

    sealed class Output {
        data object NavigateBack: Output()
        data class LogInSuccess(val cookies: List<CookieModel>): Output()
    }

    data class State(
        val login: String = "",
        val password: String = "",
        val loading: Boolean = false,
        val success: Boolean = true
    )
}