package ru.blays.ficbookReader.components.main

import androidx.compose.runtime.Composable
import ru.blays.ficbookReader.components.fanficsList.FanficsListContent
import ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration.FeedComponent

@Composable
fun MainFeedContent(component: FeedComponent) {
    FanficsListContent(component.fanficListComponent)
}