package ru.blays.ficbook.reader.shared.components.mainScreenComponents.declaration

import com.arkivanov.decompose.value.Value
import ru.blays.ficbook.reader.shared.data.dto.FanficCardModelStable

interface SavedFanficsComponent {
    val state: Value<List<FanficCardModelStable>>

    fun onOutput(output: Output)

    sealed class Output {
        data class NavigateToFanficPage(val href: String): Output()
    }
}