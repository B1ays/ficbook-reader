package ru.blays.ficbook.api.parsers

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jsoup.nodes.Document
import org.jsoup.select.Evaluator

internal class ChapterParser: IDataParser<Document, String> {
    override suspend fun parse(data: Document): String = coroutineScope {
        val textElements = data.select(
            Evaluator.Class("js-part-text part_text clearfix js-public-beta-text js-bookmark-area")
        )
        val builder = textElements.fold(StringBuilder()) { builder, element ->
            builder.append(element.wholeText())
        }
        return@coroutineScope builder.toString()
    }

    override fun parseSynchronously(data: Document): StateFlow<String?> {
        val resultFlow = MutableStateFlow<String?>(null)
        launch {
            resultFlow.value = parse(data)
        }
        return resultFlow
    }
}