@file:JvmName("FanficReaderContentCommon")

package ru.blays.ficbook.components.fanficPage.reader

import androidx.compose.runtime.Composable
import ru.blays.ficbook.reader.shared.components.readerComponents.declaration.MainReaderComponent

@Composable
expect fun FanficReaderContent(component: MainReaderComponent)

infix fun Int.percentageOf(value: Int): Int = ((this*100F)/value.toFloat()).toInt()