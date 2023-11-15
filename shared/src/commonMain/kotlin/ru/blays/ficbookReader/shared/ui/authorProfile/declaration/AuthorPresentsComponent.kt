package ru.blays.ficbookReader.shared.ui.authorProfile.declaration

import com.arkivanov.decompose.value.Value
import ru.blays.ficbookReader.shared.data.dto.AuthorPresentModelStable

interface AuthorPresentsComponent {
    val state: Value<State>

    fun onOutput(output: Output)

    sealed class Output {
        data class OpenAnotherProfile(val href: String): Output()
        data class OpenFanfic(val href: String): Output()
    }

    data class State(
        val loading: Boolean,
        val error: Boolean,
        val errorMessage: String?,
        val presents: List<AuthorPresentModelStable>
    )
}