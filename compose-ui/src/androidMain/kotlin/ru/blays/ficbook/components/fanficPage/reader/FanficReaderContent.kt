package ru.blays.ficbook.components.fanficPage.reader

import androidx.compose.runtime.Composable
import ru.blays.ficbook.components.fanficPage.reader2.FanficReaderContent2
import ru.blays.ficbook.reader.shared.components.readerComponents.declaration.MainReaderComponent

@Composable
actual fun FanficReaderContent(component: MainReaderComponent) {
    FanficReaderContent2(component)
}