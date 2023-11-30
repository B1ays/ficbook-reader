package ru.blays.ficbookReader.shared.ui.usersComponent.declaration

import com.arkivanov.decompose.value.Value
import ru.blays.ficbookReader.shared.data.dto.UserModelStable

interface UsersFavouriteComponent {
    val state: Value<State>

    fun sendIntent(intent: Intent)
    fun onOutput(output: UsersRootComponent.Output)

    sealed class Intent {
        data object LoadNextPage: Intent()
    }

    data class State(
        val list: List<UserModelStable>,
        val loading: Boolean,
        val error: Boolean,
        val errorMessage: String?
    )
}