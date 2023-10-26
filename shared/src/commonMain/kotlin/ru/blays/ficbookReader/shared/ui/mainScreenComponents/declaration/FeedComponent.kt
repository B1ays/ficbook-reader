package ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration

import ru.blays.ficbookReader.shared.ui.fanficListComponents.FanficsListComponent
import ru.blays.ficbookapi.data.SectionWithQuery

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