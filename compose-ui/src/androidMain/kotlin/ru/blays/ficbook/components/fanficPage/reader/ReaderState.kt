package ru.blays.ficbook.components.fanficPage.reader

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Stable
data class ReaderState(
    private val text: AnnotatedString,
    private var config: TextSplitterConfig.SinglePanelConfig,
    private val measurer: TextMeasurer,
    private val scope: CoroutineScope,
    private var initialCharIndex: Int
) {
    private val _pages: MutableState<List<AnnotatedString>> = mutableStateOf(
        splitTextToPages(
            text = text,
            config = config,
            textMeasurer = measurer
        )
    )

    val pages: List<AnnotatedString> by _pages

    private val _pageIndex: MutableIntState = mutableIntStateOf(getPageForCharIndex(pages))

    val pageIndex: Int by _pageIndex
    val pagesCount: Int get() = pages.size

    val hasPreviousPage by derivedStateOf { pageIndex > 0 }
    val hasNextPage by derivedStateOf { pageIndex < pages.lastIndex }

    fun scrollToPage(pageIndex: Int) { _pageIndex.intValue = pageIndex }

    fun previousPage() = scrollToPage(pageIndex - 1)

    fun nextPage() = scrollToPage(pageIndex + 1)

    private var configUpdateJob: Job? = null
    fun updateConfig(config: TextSplitterConfig.SinglePanelConfig) {
        configUpdateJob?.cancel()
        configUpdateJob = scope.launch {
            delay(500)
            if (pagesCount > 0) {
                this@ReaderState.initialCharIndex = getMiddleCharIndex()
                this@ReaderState.config = config
                val newPages = splitTextToPages(
                    text = text,
                    config = config,
                    textMeasurer = measurer
                )
                val newPageIndex = getPageForCharIndex(newPages)
                _pages.value = newPages
                scrollToPage(newPageIndex)
            } else {
                this@ReaderState.config = config
                val newPages = splitTextToPages(
                    text = text,
                    config = config,
                    textMeasurer = measurer
                )
                _pages.value = newPages
            }
        }
    }

    internal fun getMiddleCharIndex(): Int {
        return pages
            .subList(0, pageIndex)
            .sumOf(AnnotatedString::length) +
                (pages[pageIndex].length / 2)
    }

    private fun getPageForCharIndex(pages: List<AnnotatedString>): Int {
        var currentIndex = 0
        for(i in pages.indices) {
            val page = pages[i]
            currentIndex += page.length
            if(currentIndex >= initialCharIndex) {
                return i
            }
        }
        return -1
    }

    companion object {
        fun saver(
            text: AnnotatedString,
            config: TextSplitterConfig.SinglePanelConfig,
            measurer: TextMeasurer,
            scope: CoroutineScope,
            onDispose: (charIndex: Int) -> Unit
        ) = Saver<ReaderState, Int>(
            save = { state ->
                if(state.pages.isNotEmpty()) {
                    state.getMiddleCharIndex().also(onDispose)
                } else {
                    state.initialCharIndex
                }
            },
            restore = { charIndex ->
                ReaderState(
                    text = text,
                    config = config,
                    measurer = measurer,
                    scope = scope,
                    initialCharIndex = charIndex
                )
            }
        )
    }
}

@Composable
fun rememberReaderState(
    vararg inputs: Any,
    text: AnnotatedString,
    initialCharIndex: Int,
    config: TextSplitterConfig.SinglePanelConfig,
    onDispose: (charIndex: Int) -> Unit
): ReaderState {
    val measurer = rememberTextMeasurer()
    val scope = rememberCoroutineScope()

    val state = rememberSaveable(
        text,
        *inputs,
        saver = ReaderState.saver(
            text = text,
            config = config,
            measurer = measurer,
            scope = scope,
            onDispose = onDispose
        )
    ) {
        ReaderState(
            text = text,
            config = config,
            measurer = measurer,
            scope = scope,
            initialCharIndex = initialCharIndex
        )
    }

    LaunchedEffect(config) {
        state.updateConfig(config)
    }

    DisposableEffect(state) {
        onDispose {
            if(state.pagesCount > 0) {
                val charIndex = state.getMiddleCharIndex()
                onDispose(charIndex)
            }
        }
    }

    return state
}
