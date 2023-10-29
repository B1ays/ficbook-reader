package ru.blays.ficbookReader.components.fanficPage.reader

import androidx.compose.runtime.*
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import kotlinx.coroutines.*

@Composable
fun rememberTextPages(
    text: String,
    config: TextSplitterConfig.SinglePanelConfig
): List<String> {
    val textMeasurer = rememberTextMeasurer()
    val pages = remember {
        mutableStateListOf<String>()
    }
    val scope = rememberCoroutineScope { Dispatchers.IO }
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
    val scope = rememberCoroutineScope { Dispatchers.IO }
    var job by remember { mutableStateOf<Job?>(null) }

    DisposableEffect(key1 = text, key2 = config) {
        println("Start two panel page splitter")
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

private suspend fun calculatePages(
    measureResult: TextLayoutResult,
    maxHeight: Int,
    text: String
): List<String> = coroutineScope {
    val newPages = mutableListOf<String>()
    var currentHeight = 0F
    var currentPage = ""

    for (line in 0 until measureResult.lineCount) {
        yield()
        currentHeight += measureResult.multiParagraph.getLineHeight(line)
        if (currentHeight >= maxHeight) {
            newPages += currentPage
            val lineStart = measureResult.getLineStart(line)
            val lineEnd = measureResult.getLineEnd(line)
                .coerceAtMost(text.lastIndex)

            currentPage = text
                .substring(lineStart .. lineEnd)
                .run { if(last() == '\n') this else dropLast(1) }
            currentHeight = measureResult.multiParagraph.getLineHeight(line)
        } else {
            val lineStart = measureResult.getLineStart(line)
            val lineEnd = measureResult
                .getLineEnd(line)
                .coerceAtMost(text.lastIndex)

            currentPage += text
                .substring(lineStart .. lineEnd)
                .run { if(lastOrNull() == '\n') this else dropLast(1) }
        }
    }

    if(currentPage.isNotEmpty()) {
        newPages += currentPage
    }
    return@coroutineScope newPages
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