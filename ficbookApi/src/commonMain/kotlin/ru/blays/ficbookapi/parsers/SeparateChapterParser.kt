package ru.blays.ficbookapi.parsers

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jsoup.nodes.Document
import org.jsoup.select.Evaluator

internal class SeparateChapterParser: IDataParser<Document, String> {
    private val chapterTextParser = ChapterTextParser()
    override suspend fun parse(data: Document): String = coroutineScope {
        val textElements = data.select(
            Evaluator.Class("js-part-text part_text clearfix js-public-beta-text js-bookmark-area")
        )
        return@coroutineScope chapterTextParser.parse(textElements)
    }

    override fun parseSynchronously(data: Document): StateFlow<String?> {
        val resultFlow = MutableStateFlow<String?>(null)
        launch {
            resultFlow.value = parse(data)
        }
        return resultFlow
    }
}