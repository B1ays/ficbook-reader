package ru.blays.ficbook.api.parsers

import kotlinx.coroutines.coroutineScope
import org.jsoup.nodes.Element
import ru.blays.ficbook.api.ATTR_HREF
import ru.blays.ficbook.api.dataModels.FandomModel

internal class FandomParser {
    suspend fun parse(data: Element): List<FandomModel> = coroutineScope {
        return@coroutineScope data.select(".mb-15").map { mb15 ->
            mb15.select("a").let { a ->
                FandomModel(
                    href = a.attr(ATTR_HREF),
                    name = a.text(),
                    description = mb15.select(".text-muted").text()
                )
            }
        }
    }
}
