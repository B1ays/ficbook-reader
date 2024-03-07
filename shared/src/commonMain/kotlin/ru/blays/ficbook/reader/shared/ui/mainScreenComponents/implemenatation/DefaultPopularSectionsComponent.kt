package ru.blays.ficbook.reader.shared.ui.mainScreenComponents.implemenatation

import com.arkivanov.decompose.ComponentContext
import ru.blays.ficbook.reader.shared.data.dto.FanficDirection
import ru.blays.ficbook.reader.shared.data.dto.SectionWithQuery
import ru.blays.ficbook.reader.shared.data.sections.popularSections
import ru.blays.ficbook.reader.shared.ui.mainScreenComponents.declaration.PopularSectionsComponent

class DefaultPopularSectionsComponent(
    componentContext: ComponentContext,
    private val onOutput: (output: PopularSectionsComponent.Output) -> Unit
): PopularSectionsComponent, ComponentContext by componentContext {
    override val sections: List<Pair<SectionWithQuery, FanficDirection>> = with(popularSections) {
        listOf(
            gen to FanficDirection.GEN,
            het to FanficDirection.HET,
            slash to FanficDirection.SLASH,
            femslash to FanficDirection.FEMSLASH,
            article to FanficDirection.ARTICLE,
            mixed to FanficDirection.MIXED,
            other to FanficDirection.OTHER
        )
    }

    override fun onOutput(output: PopularSectionsComponent.Output) {
        onOutput.invoke(output)
    }
}