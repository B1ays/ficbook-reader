package ru.blays.ficbook.reader.shared.components.profileComponents.declaration

import kotlinx.coroutines.flow.StateFlow
import ru.blays.ficbook.reader.shared.data.dto.SavedUserModel

interface UserProfileComponent {
    val state: StateFlow<SavedUserModel?>

    fun sendIntent(intent: Intent)
    fun onOutput(output: Output)

    sealed class Intent {
        data object EnableIncognito : Intent()
    }

    sealed class Output {
        data object NavigateBack: Output()
        data object ManageAccounts: Output()
        class OpenProfile: Output() {
            internal var href: String = ""
        }
    }
}