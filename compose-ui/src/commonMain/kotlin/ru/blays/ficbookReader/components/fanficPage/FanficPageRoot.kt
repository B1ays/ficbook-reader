package ru.blays.ficbookReader.components.fanficPage

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import ru.blays.ficbookReader.components.fanficPage.reader.FanficReaderContent
import ru.blays.ficbookReader.shared.ui.fanficPageComponents.declaration.FanficPageComponent

@Composable
fun FanficPageContent(component: FanficPageComponent) {
    Children(
        stack = component.childStack,
        modifier = Modifier.fillMaxSize()
    ) {
        when(val child = it.instance) {
            is FanficPageComponent.Child.Info -> FanficPageInfoContent(child.component)
            is FanficPageComponent.Child.Reader -> FanficReaderContent(child.component)
            is FanficPageComponent.Child.Comments -> TODO()
        }
    }
}