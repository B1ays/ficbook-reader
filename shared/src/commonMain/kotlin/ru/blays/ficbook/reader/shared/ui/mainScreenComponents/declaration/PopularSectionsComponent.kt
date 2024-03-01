package ru.blays.ficbook.reader.shared.ui.mainScreenComponents.declaration

import ru.blays.ficbook.reader.shared.data.dto.FanficDirection
import ru.blays.ficbook.reader.shared.data.dto.SectionWithQuery

interface PopularSectionsComponent {
    val sections: List<Pair<SectionWithQuery, FanficDirection>>

    fun onOutput(output: Output)

    sealed class Output {
        data class NavigateToSection(val section: SectionWithQuery): Output()
    }

}