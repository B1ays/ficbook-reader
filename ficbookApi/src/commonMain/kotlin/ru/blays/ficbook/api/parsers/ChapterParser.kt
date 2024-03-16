package ru.blays.ficbook.api.parsers

import kotlinx.coroutines.coroutineScope
import org.jsoup.nodes.Document
import org.jsoup.select.Evaluator

internal class ChapterParser {
    suspend fun parse(data: Document): String = coroutineScope {
        val textElements = data.select(
            Evaluator.Class("js-part-text part_text clearfix js-public-beta-text js-bookmark-area")
        )
        return@coroutineScope textElements.wholeText
    }
}