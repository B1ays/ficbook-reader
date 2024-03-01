package ru.blays.ficbook.reader.shared.ui.profileComponents.declaration

import com.arkivanov.decompose.value.Value
import ru.blays.ficbook.reader.shared.data.dto.SavedUserModel

interface UserProfileManagingComponent {
    val state: Value<State>

    fun sendIntent(intent: Intent)

    fun onOutput(output: Output)

    sealed class Intent {
        data class ChangeUser(val id: String): Intent()
        data class DeleteUser(val id: String): Intent()
        data object AddNewAccount: Intent()
    }

    sealed class Output {
        data object NavigateBack: Output()
        data object AddNewAccount: Output()
    }

    data class State(
        val savedUsers: List<SavedUserModel>,
        val selectedUserID: String?
    )
}