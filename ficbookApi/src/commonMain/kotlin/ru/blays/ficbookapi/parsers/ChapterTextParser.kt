package ru.blays.ficbookapi.parsers

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.apache.commons.text.StringEscapeUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Entities
import org.jsoup.safety.Safelist
import org.jsoup.select.Elements

class ChapterTextParser: IDataParser<Elements, String> {
    private val outputSettings = Document.OutputSettings().apply {
        prettyPrint(false)
        escapeMode(Entities.EscapeMode.extended)
    }
    override suspend fun parse(data: Elements): String = coroutineScope {
        val cleanedString = Jsoup.clean(
            data.html(),
            "",
            Safelist.none(),
            outputSettings
        )
        return@coroutineScope StringEscapeUtils
            .unescapeJava(cleanedString)
            .replace("&nbsp;", "\u00A0")
    }

    override fun parseSynchronously(data: Elements): StateFlow<String?> {
        val resultFlow = MutableStateFlow<String?>(null)
        launch {
            resultFlow.value = parse(data)
        }
        return resultFlow
    }
}