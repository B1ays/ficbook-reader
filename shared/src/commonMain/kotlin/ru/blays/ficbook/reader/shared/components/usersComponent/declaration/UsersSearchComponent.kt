package ru.blays.ficbook.reader.shared.components.usersComponent.declaration

import com.arkivanov.decompose.value.Value
import ru.blays.ficbook.reader.shared.data.dto.UserModelStable

interface UsersSearchComponent {
    val state: Value<State>

    fun sendIntent(intent: Intent)
    fun onOutput(output: UsersRootComponent.Output)

    sealed class Intent {
        data object Clear: Intent()
        data class ChangeSearchedName(val newName: String): Intent()
    }

    data class State(
        val searchedName: String,
        val list: List<UserModelStable>,
        val loading: Boolean,
        val error: Boolean,
        val errorMessage: String?
    )
}