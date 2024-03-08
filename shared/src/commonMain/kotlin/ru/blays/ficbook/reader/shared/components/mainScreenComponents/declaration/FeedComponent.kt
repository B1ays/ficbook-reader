package ru.blays.ficbook.reader.shared.components.mainScreenComponents.declaration

import ru.blays.ficbook.api.data.SectionWithQuery
import ru.blays.ficbook.reader.shared.components.fanficListComponents.declaration.FanficsListComponent

interface FeedComponent {

    val fanficListComponent: FanficsListComponent

    fun onIntent(intent: Intent)

    sealed class Intent {
        data class SetFeedSection(val section: SectionWithQuery): Intent()
    }
}

internal interface FeedComponentInternal: FeedComponent {
    fun refresh()
}