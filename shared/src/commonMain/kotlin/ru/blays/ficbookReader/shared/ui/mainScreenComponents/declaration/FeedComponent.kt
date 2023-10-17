package ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration

import com.arkivanov.decompose.value.Value
import ru.blays.ficbookReader.shared.ui.fanficListComponents.FanficsListComponent
import ru.blays.ficbookapi.data.SectionWithQuery

interface FeedComponent {

    val state: Value<SectionWithQuery>

    val fanficListComponent: FanficsListComponent

    fun onIntent(intent: Intent)

    sealed class Intent {
        data class ChangeFeedSection(val section: SectionWithQuery): Intent()
    }
}

internal interface FeedComponentInternal: FeedComponent {
    fun setState(value: SectionWithQuery)
    fun refresh()
}