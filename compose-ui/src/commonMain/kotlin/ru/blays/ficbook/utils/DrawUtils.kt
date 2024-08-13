package ru.blays.ficbook.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.nativeCanvas

inline fun ContentDrawScope.drawWithLayer(
    block: ContentDrawScope.() -> Unit
) {
    with(drawContext.canvas.nativeCanvas) {
        val checkPoint = saveLayer(null, null)
        block()
        restoreToCount(checkPoint)
    }
}

inline fun Modifier.drawWithLayer(
    crossinline block: ContentDrawScope.() -> Unit
) = drawWithContent {
    drawWithLayer(block)
}