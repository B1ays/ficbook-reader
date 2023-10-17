package ru.blays.ficbookReader.shared.ui.mainScreenComponents.implemenatation

import com.arkivanov.decompose.ComponentContext
import ru.blays.ficbookReader.shared.data.dto.FanficDirection
import ru.blays.ficbookReader.shared.data.dto.Section
import ru.blays.ficbookReader.shared.data.sections.PopularSections
import ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration.PopularSectionsComponent


class DefaultPopularSectionsComponent(
    componentContext: ComponentContext,
    private val onOutput: (output: PopularSectionsComponent.Output) -> Unit
): PopularSectionsComponent, ComponentContext by componentContext {
    override val sections: List<Pair<Section, FanficDirection>> = with(PopularSections) {
        listOf(
            gen to FanficDirection.GEN,
            het to FanficDirection.HET,
            slash to FanficDirection.SLASH,
            femslash to FanficDirection.FEMSLASH,
            mixed to FanficDirection.MIXED,
            other to FanficDirection.OTHER
        )
    }


    override fun onOutput(output: PopularSectionsComponent.Output) {
        onOutput.invoke(output)
    }
}