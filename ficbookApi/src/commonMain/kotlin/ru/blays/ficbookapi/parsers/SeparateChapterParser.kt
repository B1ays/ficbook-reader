package ru.blays.ficbookapi.parsers

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jsoup.nodes.Document
import org.jsoup.nodes.Entities
import org.jsoup.select.Evaluator

class SeparateChapterParser: IDataParser<Document, String> {
    private val outputSettings = Document.OutputSettings().apply {
        prettyPrint(false)
        escapeMode(Entities.EscapeMode.extended)
    }

    override suspend fun parse(data: Document): String = coroutineScope {
        data.outputSettings(outputSettings)
        val chapterTextParser = ChapterTextParser()

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