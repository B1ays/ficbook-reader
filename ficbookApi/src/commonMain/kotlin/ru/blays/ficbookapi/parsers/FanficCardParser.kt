package ru.blays.ficbookapi.parsers

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.jsoup.select.Evaluator
import ru.blays.ficbookapi.dataModels.FanficCompletionStatus
import ru.blays.ficbookapi.dataModels.FanficDirection
import ru.blays.ficbookapi.dataModels.FanficModel
import ru.blays.ficbookapi.dataModels.FanficRating
import ru.blays.ficbookapi.dataModels.FanficStatus
import ru.blays.ficbookapi.dataModels.FanficTag
import ru.blays.ficbookapi.dataModels.ReadBadgeModel

internal class FanficsListParser: IDataParser<String, Elements> {
    override suspend fun parse(data: String): Elements = coroutineScope {
        val document = Jsoup.parse(data)
        return@coroutineScope document.select(".js-toggle-description")
    }

    override fun parseSynchronously(data: String): StateFlow<Elements?> {
        val resultFlow = MutableStateFlow<Elements?>(null)
        launch {
            resultFlow.value = parse(data)
        }
        return resultFlow
    }
}

internal class FanficCardParser: IDataParser<Element, FanficModel> {
    override suspend fun parse(data: Element): FanficModel = coroutineScope {
        val fanficMainInfo = data
            .select(".fanfic-main-info")
            .firstOrNull()

        val fanficInlineInfo = data
            .select(".fanfic-inline-info")

        val (href, title) = with(
            data.select(".fanfic-inline-title").select(".visit-link")
        ) {
            attr("href") to text()
        }

        val likes = fanficMainInfo
            ?.select(
                Evaluator.Class("badge-with-icon badge-secondary badge-like")
            )
            ?.select(".badge-text")
            ?.text()
            ?.toIntOrNull()
            ?: 0

        val trophies = fanficMainInfo
            ?.select(
                Evaluator.Class("badge-with-icon badge-secondary badge-reward")
            )
            ?.select(".badge-text")
            ?.text()
            ?.toIntOrNull()
            ?: 0

        val rating = fanficMainInfo?.run {
            val divs = select("div:not(.fanfic-main-info)")

            for(div in divs) {
                if(div.outerHtml().contains("badge-rating")) {
                    return@run FanficRating.getForName(div.text())
                }
            }
            return@run null
        } ?: FanficRating.UNKNOWN

        val direction = fanficMainInfo?.run {
            val divs = select("div:not(.fanfic-main-info)")

            for(div in divs) {
                if(div.outerHtml().contains("direction")) {
                    return@run FanficDirection.getForName(div.text())
                }
            }
            return@run null
        } ?: FanficDirection.UNKNOWN

        val status = fanficMainInfo?.run {
            val divs = select("div:not(.fanfic-main-info)")

            for(div in divs) {
                if(div.outerHtml().contains("status")) {
                    return@run FanficCompletionStatus.getForName(div.text())
                }
            }
            return@run null
        } ?: FanficCompletionStatus.UNKNOWN

        val isHot = fanficMainInfo
            ?.run {
                val hotFanficClass = select(".hot-fanfic")
                !hotFanficClass.isEmpty()
            }
            ?: false

        val readInfo = fanficMainInfo?.run {
            val badge = select(".read-notification")
            val readDate = badge.select(".hidden-xs").text()
            val hasUpdate = badge.select(".new-content").isNotEmpty()

            return@run if(readDate.isEmpty()) null
            else ReadBadgeModel(
                readDate, hasUpdate
            )
        }

        val author = data
            .select(Evaluator.Class("author word-break"))
            .text()
            ?: ""

        val fandom = fanficInlineInfo
            .firstOrNull {
                it.html().contains("Фэндом:")
            }
            ?.select("a")
            ?.text()
            ?: ""

        val updateDate = fanficInlineInfo
            .firstOrNull {
                it.text().contains("Дата обновления:") ||
                        it.text().contains("Дата завершения:") ||
                        it.text().contains("Дата создания:")
            }
            ?.select("dd")
            ?.text()
            ?: ""


        val tagsElements = data
            .select(".tags")
            .select("a")

        val tags = mutableListOf<FanficTag>().apply {
            tagsElements.forEach {
                val tagHref = it.attr("href")
                val name = it.text()
                val isAdult = it.outerHtml().contains("tag-adult")
                add(
                    FanficTag(
                        name = name,
                        isAdult = isAdult,
                        href = tagHref
                    )
                )
            }
        }

        val description = data.select(".fanfic-description").text()

        val coverUrl = data
            .select(".side-Section")
            .select("source")
            .attr("srcset")

        return@coroutineScope FanficModel(
            href = href,
            title = title,
            status = FanficStatus(
                direction = direction,
                rating = rating,
                status = status,
                hot = isHot,
                likes = likes,
                trophies = trophies
            ),
            author = author,
            fandom = fandom,
            updateDate = updateDate,
            tags = tags,
            description = description,
            coverUrl = coverUrl,
            readInfo = readInfo
        )
    }

    override fun parseSynchronously(data: Element): StateFlow<FanficModel?> {
        val resultFlow = MutableStateFlow<FanficModel?>(null)
        launch {
            resultFlow.value = parse(data)
        }
        return resultFlow
    }
}
