package ru.blays.ficbook.components.fanficPage.reader2

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import ru.blays.ficbook.components.fanficPage.reader.TextSplitterConfig
import ru.blays.ficbook.components.fanficPage.reader.splitTextToPages

@Stable
data class ReaderState(
    private val text: AnnotatedString,
    private val config: TextSplitterConfig.SinglePanelConfig,
    private val measurer: TextMeasurer,
    private val initialCharIndex: Int
) {
    init {
        println("Created new ReaderState")
    }

    val pages: List<AnnotatedString> = splitTextToPages(
        text = text,
        config = config,
        textMeasurer = measurer
    )

    private val _pageIndex: MutableIntState = mutableIntStateOf(getPageForCharIndex())

    val pageIndex: Int by _pageIndex
    val pagesCount: Int = pages.size

    val hasPreviousPage by derivedStateOf { pageIndex > 0 }
    val hasNextPage by derivedStateOf { pageIndex < pages.lastIndex }

    fun scrollToPage(pageIndex: Int) {
        _pageIndex.value = pageIndex
    }

    fun previousPage() = scrollToPage(pageIndex - 1)

    fun nextPage() = scrollToPage(pageIndex + 1)

    private fun getMiddleCharIndex(): Int {
        return pages
            .subList(0, pageIndex)
            .sumOf(AnnotatedString::length) + (pages[pageIndex].length / 2)
    }

    private fun getPageForCharIndex(): Int {
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
            measurer: TextMeasurer
        ) = Saver<ReaderState, Any>(
            save = { state ->
                if(state.pages.isNotEmpty()) {
                    state.getMiddleCharIndex()
                } else {
                    state.initialCharIndex
                }
            },
            restore = { charIndex ->
                ReaderState(
                    text = text,
                    config = config,
                    measurer = measurer,
                    initialCharIndex = charIndex as Int

                )
            }
        )
    }
}

@Composable
fun rememberReaderState(
    vararg inputs: Any,
    text: AnnotatedString,
    config: TextSplitterConfig.SinglePanelConfig
): ReaderState {
    val measurer = rememberTextMeasurer()
    return rememberSaveable(
        text,
        config,
        inputs,
        saver = ReaderState.saver(text, config, measurer)
    ) {
        ReaderState(text, config, measurer, 0)
    }
}
