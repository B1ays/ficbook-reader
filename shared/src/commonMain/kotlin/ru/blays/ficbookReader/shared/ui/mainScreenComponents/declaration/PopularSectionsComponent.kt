package ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration

import ru.blays.ficbookReader.shared.data.dto.FanficDirection
import ru.blays.ficbookReader.shared.data.dto.Section

interface PopularSectionsComponent {
    val sections: List<Pair<Section, FanficDirection>>

    fun onOutput(output: Output)

    sealed class Output {
        data class NavigateToSection(val section: Section): Output()
        data object NavigateBack: Output()
    }

}