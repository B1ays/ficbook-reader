package ru.blays.ficbookReader.shared.ui.profileComponents

import kotlinx.coroutines.flow.StateFlow
import ru.blays.ficbookReader.shared.data.dto.UserModelStable

interface UserProfileComponent {
    val state: StateFlow<UserModelStable?>

    fun sendIntent(intent: Intent)
    fun onOutput(output: Output)

    sealed class Intent {
        data object LogOut: Intent()
    }

    sealed class Output {
        data object NavigateBack: Output()
        data class OpenProfile(val href: String): Output()
    }
}