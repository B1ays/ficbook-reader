package ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration

import ru.blays.ficbookReader.shared.data.dto.FanficDirection
import ru.blays.ficbookReader.shared.data.dto.SectionWithQuery

interface PopularSectionsComponent {
    val sections: List<Pair<SectionWithQuery, FanficDirection>>

    fun onOutput(output: Output)

    sealed class Output {
        data class NavigateToSection(val section: SectionWithQuery): Output()
    }

}