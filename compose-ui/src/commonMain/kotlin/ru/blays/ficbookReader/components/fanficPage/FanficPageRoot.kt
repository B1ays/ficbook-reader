package ru.blays.ficbookReader.components.fanficPage

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import ru.blays.ficbookReader.components.commentsContent.CommentsScreenContent
import ru.blays.ficbookReader.components.commentsContent.PartCommentsContent
import ru.blays.ficbookReader.components.fanficPage.reader.FanficReaderContent
import ru.blays.ficbookReader.shared.ui.fanficPageComponents.declaration.FanficPageComponent
import ru.blays.ficbookReader.utils.LocalStackAnimator

@Composable
fun FanficPageContent(component: FanficPageComponent) {
    val animator = LocalStackAnimator.current
    Children(
        stack = component.childStack,
        modifier = Modifier.fillMaxSize(),
        animation = stackAnimation(animator)
    ) {
        when(val child = it.instance) {
            is FanficPageComponent.Child.Info -> FanficPageInfoContent(child.component)
            is FanficPageComponent.Child.Reader -> FanficReaderContent(child.component)
            is FanficPageComponent.Child.PartComments -> PartCommentsContent(child.component)
            is FanficPageComponent.Child.AllComments -> CommentsScreenContent(child.component)
            is FanficPageComponent.Child.DownloadFanfic -> FanficDownloadContent(child.component)
        }
    }
}