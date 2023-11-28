package ru.blays.ficbookapi.parsers

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jsoup.select.Elements

internal class ChapterTextParser: IDataParser<Elements, String> {
    override suspend fun parse(data: Elements): String = coroutineScope {
        val builder = data.fold(StringBuilder()) { builder, element ->
            builder.append(element.wholeText())
        }
        return@coroutineScope builder.toString()
    }

    override fun parseSynchronously(data: Elements): StateFlow<String?> {
        val resultFlow = MutableStateFlow<String?>(null)
        launch {
            resultFlow.value = parse(data)
        }
        return resultFlow
    }
}