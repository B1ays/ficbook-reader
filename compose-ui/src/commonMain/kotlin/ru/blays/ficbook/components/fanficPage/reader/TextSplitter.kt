package ru.blays.ficbook.components.fanficPage.reader

import androidx.compose.runtime.*
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.floor

@Composable
fun rememberTextPages(
    text: String,
    config: TextSplitterConfig.SinglePanelConfig
): List<String> {
    val textMeasurer = rememberTextMeasurer()
    val pages = remember {
        mutableStateListOf<String>()
    }
    val scope = rememberCoroutineScope { Dispatchers.Default }
    var job: Job? by remember { mutableStateOf(null) }

    DisposableEffect(key1 = text, key2 = config) {
        job = scope.launch {
            val newPages = mutableListOf<String>()
            val measureResult = textMeasurer.measure(
                text = text,
                style = config.style,
                constraints = config.constraints
            )
            val maxHeight = config.constraints.maxHeight

            newPages += calculatePages(
                measureResult = measureResult,
                maxHeight = maxHeight,
                text = text
            )

            pages.clear()
            pages.addAll(newPages)
        }
        onDispose {
            job?.cancel()
        }
    }

    return pages
}

@Composable
fun rememberTwoPanelTextPages(
    text: String,
    config: TextSplitterConfig.TwoPanelConfig
): List<Pair<String, String>> {
    val textMeasurer = rememberTextMeasurer()
    val pages = remember {
        mutableStateListOf<Pair<String, String>>()
    }
    val scope = rememberCoroutineScope { Dispatchers.Default }
    var job by remember { mutableStateOf<Job?>(null) }

    DisposableEffect(key1 = text, key2 = config) {
        job = scope.launch {
            val newPages = mutableListOf<String>()

            val panelWidth = with(config) { (constraints.maxWidth - spaceBetweenPanel)/2 }

            val resultConstraints = config.constraints.copy(
                maxWidth = panelWidth,
                minWidth = panelWidth
            )

            val measureResult = textMeasurer.measure(
                text = text,
                style = config.style,
                constraints = resultConstraints
            )
            val maxHeight = resultConstraints.maxHeight

            newPages += calculatePages(
                measureResult = measureResult,
                maxHeight = maxHeight,
                text = text
            )

            val twoPanelPages = newPages.run {
                val pagePairs = mutableListOf<Pair<String, String>>()
                for (i in indices step 2) {
                    val firstPanel = getOrElse(i) { "" }
                    val secondPanel = getOrElse(i+1) { "" }
                    pagePairs += firstPanel to secondPanel
                }
                return@run pagePairs
            }

            pages.clear()
            pages.addAll(twoPanelPages)
        }

        onDispose {
            job?.cancel()
        }
    }

    return pages
}

suspend fun splitTextToPages(
    text: String,
    config: TextSplitterConfig.SinglePanelConfig,
    textMeasurer: TextMeasurer
): List<String> = coroutineScope {
    val measureResult = textMeasurer.measure(
        text = text,
        style = config.style,
        constraints = config.constraints
    )
    val maxHeight = config.constraints.maxHeight

    val result = calculatePages(
        measureResult = measureResult,
        maxHeight = maxHeight,
        text = text
    )
    return@coroutineScope result
}

private suspend fun calculatePages(
    measureResult: TextLayoutResult,
    maxHeight: Int,
    text: String
): List<String> = coroutineScope {
    val textLastIndex = text.lastIndex
    return@coroutineScope if(textLastIndex > 0) {
        val linesInHeight = floor(
            maxHeight / measureResult.multiParagraph.getLineHeight(0).toDouble()
        ).toInt()

        val lines = (0 until measureResult.lineCount).map {
            val lineStart = measureResult.getLineStart(it)
            val lineEnd = (measureResult.getLineEnd(it)-1).coerceIn(0, textLastIndex)
            text.substring(lineStart .. lineEnd)
        }

        lines.chunked(linesInHeight).map {
            it.fold("") { acc, line -> acc + line }
        }
    } else {
        emptyList()
    }
}

sealed class TextSplitterConfig {
    data class SinglePanelConfig(
        override val style: TextStyle,
        override val constraints: Constraints
    ): TextSplitterConfig()
    data class TwoPanelConfig(
        override val style: TextStyle,
        override val constraints: Constraints,
        val spaceBetweenPanel: Int
    ): TextSplitterConfig()

    abstract val style: TextStyle
    abstract val constraints: Constraints
}