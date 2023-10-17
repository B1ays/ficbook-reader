package ru.blays.ficbookapi.parsers

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jsoup.nodes.Element
import ru.blays.ficbookapi.dataModels.FandomModel

internal class FandomParser: IDataParser<Element, List<FandomModel>> {
    override suspend fun parse(data: Element): List<FandomModel> = coroutineScope {
        val mb15 = data.select(".mb-15")
        val fandomsList = mutableListOf<FandomModel>()

        mb15.forEach {
            val a = it.select("a")
            val href = a.attr("href")
            val name = a.text()
            val description = it.select(".text-muted").text()
            fandomsList += FandomModel(
                href = href,
                name = name,
                description = description
            )
        }

        return@coroutineScope fandomsList
    }

    override fun parseSynchronously(data: Element): StateFlow<List<FandomModel>> {
        val resultFlow = MutableStateFlow<List<FandomModel>>(emptyList())
        launch {
            resultFlow.value = parse(data)
        }
        return resultFlow
    }
}
