package ru.blays.ficbook.api.parsers

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.jsoup.select.Evaluator
import ru.blays.ficbook.api.ATTR_HREF
import ru.blays.ficbook.api.dataModels.*
import java.net.URLDecoder

internal class FanficsListParser: IDataParser<Document, Elements> {
    override suspend fun parse(data: Document): Elements = coroutineScope {
        return@coroutineScope data.select(".js-toggle-description")
    }

    override fun parseSynchronously(data: Document): StateFlow<Elements?> {
        val resultFlow = MutableStateFlow<Elements?>(null)
        launch {
            resultFlow.value = parse(data)
        }
        return resultFlow
    }
}

internal class FanficCardParser: IDataParser<Element, FanficCardModel> {
    override suspend fun parse(data: Element): FanficCardModel = coroutineScope {
        val fanficMainInfo = data
            .select(".fanfic-main-info")
            .firstOrNull()

        val fanficInlineInfo = data
            .select(".fanfic-inline-info")

        val (href, title) = with(
            data.select(".fanfic-inline-title").select(".visit-link")
        ) {
            val fullHref = attr(ATTR_HREF)
            val clearedHref = fullHref.substringBefore('?')
            val name =  text()
            Pair(clearedHref, name)
        }

        val id = fanficMainInfo?.select(".side-section")?.select("fanfic-more-dropdown")?.attr(":fanfic-id") ?: ""

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

        val authors = data.select(Evaluator.Class("author word-break"))

        val author = authors.firstOrNull()
            .let {
                UserModel(
                    name = it?.text() ?: "",
                    href = it?.select("a")?.attr(ATTR_HREF) ?: ""
                )
            }

        val originalAuthor = authors.getOrNull(1)
            ?.let {
                UserModel(
                    name = it.text(),
                    href = it.select("a").attr(ATTR_HREF)
                )
            }

        val fandom: List<FandomModel> = fanficInlineInfo.run {
            forEach { element ->
                if (element.select("dt").text().contains("Фэндом:")) {
                    val a = element.select("a")
                    return@run a.map {
                        FandomModel(
                            href = it.attr(ATTR_HREF),
                            name = it.text(),
                            description = ""
                        )
                    }
                }
            }
            return@run emptyList()
        }

        val pairings: List<PairingModel> = fanficInlineInfo.run {
            val list = mutableListOf<PairingModel>()

            forEach { element ->
                val dt = element.select("dt")
                if (dt.text().contains("Пэйринг и персонажи:")) {
                    val a = element.select("dd a")

                    a.forEach { aElement ->
                        val character = aElement.text()
                        val href = aElement
                            .attr(ATTR_HREF)
                            .let { URLDecoder.decode(it, "UTF-8") }

                        val isHighlighted = aElement.className().contains("pairing-highlight")

                        list += PairingModel(
                            character = character,
                            href = href,
                            isHighlighted = isHighlighted
                        )
                    }
                }
            }

                return@run list
            }

        val updateDate = fanficInlineInfo
            .firstOrNull {
                it.text().contains("Дата обновления:") ||
                it.text().contains("Дата завершения:") ||
                it.text().contains("Дата создания:")
            }
            ?.select("dd")
            ?.text()
            ?: ""

        val size = fanficInlineInfo.run {
            forEach { element ->
                val label = element.select("dt").text()
                if (label.contains("Размер:")) {
                    return@run element.select("dd").text()
                }
            }
            return@run ""
        }


        val tagsElements = data
            .select(".tags")
            .select("a")

        val tags = mutableListOf<FanficTag>().apply {
            tagsElements.forEach {
                val tagHref = it.attr(ATTR_HREF)
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

        val description = data.select(".fanfic-description")
            .fold(StringBuilder()) { acc, element ->
                acc.append(element.wholeText())
            }
            .toString()
            .trim(' ', '\n')

        val coverUrl = data
            .select(".side-Section")
            .select("source")
            .attr("srcset")
            .let { CoverUrl(it) }

        return@coroutineScope FanficCardModel(
            href = href,
            id = id,
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
            originalAuthor = originalAuthor,
            fandom = fandom,
            pairings = pairings,
            updateDate = updateDate,
            size = size,
            tags = tags,
            description = description,
            coverUrl = coverUrl,
            readInfo = readInfo
        )
    }

    override fun parseSynchronously(data: Element): StateFlow<FanficCardModel?> {
        val resultFlow = MutableStateFlow<FanficCardModel?>(null)
        launch {
            resultFlow.value = parse(data)
        }
        return resultFlow
    }
}