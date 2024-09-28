package ru.blays.ficbook.components.fanficPage.reader

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.Constraints
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.ceil
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

            newPages += calculatePages(
                layoutResult = measureResult,
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

            newPages += calculatePages(
                layoutResult = measureResult,
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

    val result = calculatePages(
        layoutResult = measureResult,
        text = text
    )
    return@coroutineScope result
}

suspend fun splitTextToPages(
    text: AnnotatedString,
    config: TextSplitterConfig.SinglePanelConfig,
    textMeasurer: TextMeasurer
): List<AnnotatedString> = coroutineScope {
    val measureResult = textMeasurer.measure(
        text = text,
        style = config.style,
        constraints = config.constraints
    )

    val result = calculatePages(
        layoutResult = measureResult,
        text = text
    )
    return@coroutineScope result
}

private suspend fun calculatePages(
    layoutResult: TextLayoutResult,
    text: String
): List<String> = coroutineScope {
    val textLastIndex = text.lastIndex
    return@coroutineScope if(textLastIndex > 0) {
        val constraints = layoutResult.layoutInput.constraints
        val lineHeight = layoutResult.multiParagraph.getLineHeight(0).toDouble()
        val linesInHeight = floor(constraints.maxHeight / lineHeight)
        val pagesCount = ceil(layoutResult.lineCount / linesInHeight).toInt()
        val pageHeight = lineHeight * linesInHeight
        val pages: MutableList<String> = mutableListOf()

        for(pageIndex in 0 until pagesCount) {
            val rect = Rect(
                left = 0F,
                right = constraints.maxWidth.toFloat(),
                top = (pageHeight * pageIndex).toFloat(),
                bottom = (pageHeight * (pageIndex + 1)).toFloat()
            )

            val range = layoutResult.multiParagraph.getRangeForRect(
                rect,
                TextGranularity.Word,
                TextInclusionStrategy.AnyOverlap
            )

            if(range == TextRange.Zero) break

            pages.add(text.substring(range.start, range.end))
        }

        pages
    } else {
        emptyList()
    }
}

private suspend fun calculatePages(
    layoutResult: TextLayoutResult,
    text: AnnotatedString
): List<AnnotatedString> = coroutineScope {
    val textLastIndex = text.lastIndex
    return@coroutineScope if(textLastIndex > 0) {
        val constraints = layoutResult.layoutInput.constraints
        val lineHeight = layoutResult.multiParagraph.getLineHeight(0).toDouble()
        val linesInHeight = floor(constraints.maxHeight / lineHeight)
        val pagesCount = ceil(layoutResult.lineCount / linesInHeight).toInt()
        val pageHeight = lineHeight * linesInHeight
        val pages: MutableList<AnnotatedString> = mutableListOf()

        for(pageIndex in 0 until pagesCount) {
            val rect = Rect(
                left = 0F,
                right = constraints.maxWidth.toFloat(),
                top = (pageHeight * pageIndex).toFloat(),
                bottom = (pageHeight * (pageIndex + 1)).toFloat()
            )

            val range = layoutResult.multiParagraph.getRangeForRect(
                rect,
                TextGranularity.Word,
                TextInclusionStrategy.AnyOverlap
            )

            if(range == TextRange.Zero) break

            pages.add(text.subSequence(range))
        }

        pages
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