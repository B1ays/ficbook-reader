package ru.blays.ficbook.components.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import ru.blays.ficbook.components.fanficsList.FanficsListContent
import ru.blays.ficbook.reader.shared.components.mainScreenComponents.declaration.FeedComponent
import ru.blays.ficbook.ui_components.FAB.ScrollToStartFAB

@Composable
fun MainFeedContent(
    component: FeedComponent,
    contentPadding: PaddingValues?
) {
    val lazyListState = rememberLazyListState()
    Scaffold(
        floatingActionButton = { ScrollToStartFAB(lazyListState) },
        floatingActionButtonPosition = FabPosition.End
    ) {
        FanficsListContent(
            component = component.fanficListComponent,
            lazyListState = lazyListState,
            contentPadding = contentPadding
        )
    }
}