package ru.blays.ficbook.components.fanficPage

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import ru.blays.ficbook.components.collectionContent.CollectionsScreenContent
import ru.blays.ficbook.components.commentsContent.CommentsScreenContent
import ru.blays.ficbook.components.commentsContent.PartCommentsContent
import ru.blays.ficbook.components.fanficPage.reader.FanficReaderContent
import ru.blays.ficbook.reader.shared.components.fanficPageComponents.declaration.FanficPageComponent
import ru.blays.ficbook.utils.defaultStackAnimation

@Composable
fun FanficPageContent(component: FanficPageComponent) {
    Children(
        stack = component.childStack,
        modifier = Modifier.fillMaxSize(),
        animation = defaultStackAnimation()
    ) {
        when(val child = it.instance) {
            is FanficPageComponent.Child.Info -> FanficPageInfoContent(child.component)
            is FanficPageComponent.Child.Reader -> FanficReaderContent(child.component)
            is FanficPageComponent.Child.PartComments -> PartCommentsContent(child.component)
            is FanficPageComponent.Child.AllComments -> CommentsScreenContent(child.component)
            is FanficPageComponent.Child.DownloadFanfic -> FanficDownloadContent(child.component)
            is FanficPageComponent.Child.AssociatedCollections -> CollectionsScreenContent(child.component)
        }
    }
}