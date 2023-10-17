package ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration

import com.arkivanov.decompose.value.Value
import ru.blays.ficbookReader.shared.data.dto.FanficCardModelStable

interface SavedFanficsComponent {
    val state: Value<List<FanficCardModelStable>>

    fun onOutput(output: Output)

    sealed class Output {
        data object NavigateBack: Output()
        data class NavigateToFanficPage(val href: String): Output()
    }
}