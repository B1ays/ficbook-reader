package ru.blays.ficbookReader.shared.ui.usersComponent.declaration

import com.arkivanov.decompose.value.Value
import ru.blays.ficbookReader.shared.data.dto.PopularAuthorModelStable

interface UsersPopularComponent {
    val state: Value<State>

    fun onOutput(output: UsersRootComponent.Output)


    data class State(
        val list: List<PopularAuthorModelStable>,
        val loading: Boolean,
        val error: Boolean,
        val errorMessage: String?
    )
}