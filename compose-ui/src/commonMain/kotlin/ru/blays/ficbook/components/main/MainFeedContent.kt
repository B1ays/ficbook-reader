package ru.blays.ficbook.components.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import ru.blays.ficbook.components.fanficsList.FanficsListContent
import ru.blays.ficbook.reader.shared.ui.mainScreenComponents.declaration.FeedComponent

@Composable
fun MainFeedContent(
    component: FeedComponent,
    contentPadding: PaddingValues?
) = FanficsListContent(
    component.fanficListComponent,
    contentPadding = contentPadding
)