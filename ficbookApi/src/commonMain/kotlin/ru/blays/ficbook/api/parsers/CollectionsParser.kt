package ru.blays.ficbook.api.parsers

import kotlinx.coroutines.coroutineScope
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.jsoup.select.Evaluator
import ru.blays.ficbook.api.ATTR_HREF
import ru.blays.ficbook.api.ATTR_VALUE
import ru.blays.ficbook.api.dataModels.*
import ru.blays.ficbook.api.notNumberRegex

internal class CollectionListParser {
    private val collectionParser = CollectionParser()
    suspend fun parse(data: Document): List<CollectionCardModel> {
        val collectionElements = data.select(
            Evaluator.Class("collection-thumb word-break js-item-wrapper")
        )

        return collectionElements.map { element ->
            collectionParser.parse(element)
        }
    }
}

internal class CollectionParser {
    suspend fun parse(data: Element): CollectionCardModel = coroutineScope {
        val a = data.select("a").firstOrNull()
        val name = a?.text() ?: ""

        val relativeID = a?.attr(ATTR_HREF)
            ?.substringAfterLast('/')
            ?: ""

        val own = data.allElements.hasClass("js-remove-collection")

        //data.select(":not(div.collection-thumb-info)").remove()

        val size = data
            .select("div.collection-thumb-info")
            .text()
            .replace(
                regex = notNumberRegex,
                replacement = ""
            )
            .toIntOrNull()
            ?: 0

        return@coroutineScope if(own) {
            val realID = data.select("span.js-remove-collection").attr("data-collection-id")
            val public = data.select("[title='Для всех']").isNotEmpty()
            CollectionCardModel.Own(
                relativeID = relativeID,
                realID = realID,
                name = name,
                size = size,
                public = public
            )
        } else {
            val realID = data.select("div[data-collection-id]").attr("data-collection-id")
            val owner = data.select("div.collection-thumb-author a").let {
                UserModel(
                    name = it.text(),
                    href = it.attr(ATTR_HREF)
                )
            }
            val subscribed = data
                .select("div.js-follow-collection-container")
                .hasAttr("style")
            CollectionCardModel.Other(
                relativeID = relativeID,
                realID = realID,
                name = name,
                size = size,
                owner = owner,
                subscribed = subscribed
            )
        }
    }
}

internal class CollectionPageParser {
    suspend fun parse(data: Document): CollectionPageModel {
        val form = data.select("form.form-inline")
        val header = data.select(Evaluator.Class("collection-header word-break"))
        val actions = data.select(".collection-action-buttons")

        val ownCollection = actions.select("form[action=/ajax/collection/delete]").isNotEmpty()

        val name = header.select("h1.mb-0").text()
        val description = header.select("p").text().takeIf(String::isNotEmpty)
        val filterParams = parseFilterParams(form)

        return if(ownCollection) {
            CollectionPageModel.Own(
                name = name,
                description = description,
                filterParams = filterParams
            )
        } else {
            val owner = header.select("a.collection-author").let {
                UserModel(
                    name = it.text(),
                    href = it.attr(ATTR_HREF)
                )
            }
            val subscribed = actions.select("span[id=fallow-collection]").hasAttr("style")
            CollectionPageModel.Other(
                name = name,
                description = description,
                owner = owner,
                subscribed = subscribed,
                filterParams = filterParams
            )
        }
    }

    private suspend fun parseFilterParams(data: Elements): CollectionFilterParams {
        val availableDirections = data
            .select("select[name=direction] option")
            .map { it.text() to it.attr(ATTR_VALUE) }
        val availableFandoms = data
            .select("select[name=fandom_id] option")
            .map { it.text() to it.attr(ATTR_VALUE) }

        return CollectionFilterParams(
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

internal class CollectionMainInfoParser {
    suspend fun parse(data: Document): CollectionMainInfoModel {
        val form = data.select("form.jsAjaxSubmitForm")
        val name = form.select("input[name=name]").attr(ATTR_VALUE)
        val description = form.select("textarea[name=description]").text()
        val public = form.select("input[name=is_public][value=1]").hasAttr("checked")

        return CollectionMainInfoModel(
            name = name,
            description = description,
            public = public
        )
    }
}
