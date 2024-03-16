package ru.blays.ficbook.api.parsers

import kotlinx.coroutines.coroutineScope
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Evaluator
import ru.blays.ficbook.api.ATTR_HREF
import ru.blays.ficbook.api.ATTR_VALUE
import ru.blays.ficbook.api.dataModels.CollectionModel
import ru.blays.ficbook.api.dataModels.CollectionSortParams
import ru.blays.ficbook.api.dataModels.UserModel
import ru.blays.ficbook.api.notNumberRegex

internal class CollectionListParser {
    private val collectionParser = CollectionParser()
    suspend fun parse(data: Document): List<CollectionModel> {
        val collectionElements = data.select(
            Evaluator.Class("collection-thumb word-break js-item-wrapper")
        )

        return collectionElements.map { element ->
            collectionParser.parse(element)
        }
    }
}

internal class CollectionParser {
    suspend fun parse(data: Element): CollectionModel = coroutineScope {
        val (href, name) = data
            .select("a")
            .run {
                val first = firstOrNull()
                if (first != null) {
                    first.attr(ATTR_HREF) to first.text()
                } else {
                    "" to ""
                }
            }
        val private = data.select("[title='Личный сборник']").isNotEmpty()
        val owner = data.select("div.collection-thumb-author").run {
            UserModel(
                name = select("a").text(),
                href = attr(ATTR_HREF)
                    .split("/")
                    .lastOrNull()
                    ?: ""
            )
        }
        data.select(":not(div.collection-thumb-info)").remove()
        val size = data
            .select("div.collection-thumb-info")
            .text()
            .replace(
                regex = notNumberRegex,
                replacement = ""
            )
            .toIntOrNull()
            ?: 0

        return@coroutineScope CollectionModel(
            href = href,
            name = name,
            size = size,
            private = private,
            owner = owner
        )
    }
}


internal class CollectionSortParamsParser {
    suspend fun parse(data: Document): CollectionSortParams {
        val form = data.select("form.form-inline")
        val availableDirections = form
            .select("select[name=direction] option")
            .map { it.text() to it.attr(ATTR_VALUE) }
        val availableFandoms = form
            .select("select[name=fandom_id] option")
            .map { it.text() to it.attr(ATTR_VALUE) }

        return CollectionSortParams(
            availableSortParams = availableSortParams,
            availableDirections = availableDirections,
            availableFandoms = availableFandoms
        )
    }

    private val availableSortParams
        get() = listOf(
            Pair("По последнему обновлению", "1"),
            Pair("По дате создания", "2"),
            Pair("По оценкам", "3"),
            Pair("По отзывам", "4"),
            Pair("По наградам", "5"),
            Pair("По порядку, предложенному автором сборника", "6"),
            Pair("По добавлению в сборник", "7")
        )
}
