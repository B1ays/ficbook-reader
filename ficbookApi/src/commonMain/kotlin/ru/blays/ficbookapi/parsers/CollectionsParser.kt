package ru.blays.ficbookapi.parsers

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Evaluator
import ru.blays.ficbookapi.dataModels.CollectionModel
import ru.blays.ficbookapi.dataModels.UserModel

class CollectionListParser: IDataParser<Document, List<CollectionModel>> {
    override suspend fun parse(data: Document): List<CollectionModel> {
        val elementsList = mutableListOf<CollectionModel>()
        val collectionElements = data.select(
            Evaluator.Class("collection-thumb word-break js-item-wrapper")
        )
        val collectionParser = CollectionParser()
        collectionElements.forEach { element ->
            elementsList += collectionParser.parse(element)
        }
        return elementsList
    }

    override fun parseSynchronously(data: Document): StateFlow<List<CollectionModel>?> {
        TODO()
    }
}

class CollectionParser: IDataParser<Element, CollectionModel> {
    override suspend fun parse(data: Element): CollectionModel = coroutineScope {
        val (href, name) = data
            .select("a")
            .run {
                val first = firstOrNull()
                if(first != null) {
                    first.attr("href") to first.text()
                } else {
                    "" to ""
                }
            }
        val private = data.select("[title='Личный сборник']").isNotEmpty()
        val owner = data.select("div.collection-thumb-author").run {
            UserModel(
                name = select("a").text(),
                href = attr("href")
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
                regex = "([() a-zA-ZА-Яа-я])+".toRegex(),
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

    override fun parseSynchronously(data: Element): StateFlow<CollectionModel?> {
        TODO()
    }

}
