package ru.blays.ficbook.api.parsers

import kotlinx.coroutines.coroutineScope
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.jsoup.select.Evaluator
import ru.blays.ficbook.api.ATTR_HREF
import ru.blays.ficbook.api.dataModels.*
import java.net.URLDecoder

internal class FanficsListParser {
    suspend fun parse(data: Document): Elements = coroutineScope {
        return@coroutineScope data.select(".js-toggle-description")
    }
}

internal class FanficCardParser {
    suspend fun parse(data: Element): FanficCardModel = coroutineScope {
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
            val name = text()
            Pair(clearedHref, name)
        }

        val id = fanficMainInfo?.select(".side-section")
            ?.select("fanfic-more-dropdown")
            ?.attr(":fanfic-id")
            ?: ""

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

            for (div in divs) {
                if (div.outerHtml().contains("badge-rating")) {
                    return@run FanficRating.getForName(div.text())
                }
            }
            return@run null
        } ?: FanficRating.UNKNOWN

        val direction = fanficMainInfo?.run {
            val divs = select("div:not(.fanfic-main-info)")

            for (div in divs) {
                if (div.outerHtml().contains("direction")) {
                    return@run FanficDirection.getForName(div.text())
                }
            }
            return@run null
        } ?: FanficDirection.UNKNOWN

        val status = fanficMainInfo?.run {
            val divs = select("div:not(.fanfic-main-info)")

            for (div in divs) {
                if (div.outerHtml().contains("status")) {
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

            return@run if (readDate.isNotEmpty()) {
                ReadBadgeModel(
                    readDate = readDate,
                    hasUpdate = hasUpdate
                )
            } else {
                null
            }
        }

        val authors = data.select(Evaluator.Class("author word-break"))

        val author = authors.firstOrNull().let {
            UserModel(
                name = it?.text() ?: "",
                href = it?.select("a")?.attr(ATTR_HREF) ?: ""
            )
        }

        val originalAuthor = authors.getOrNull(1)?.let {
            UserModel(
                name = it.text(),
                href = it.select("a").attr(ATTR_HREF)
            )
        }

        val fandom: List<FandomModel> = fanficInlineInfo.find { element ->
            element.select("dt:contains(Фэндом:)").isNotEmpty()
        }?.let { element ->
            element.select("a").map {
                FandomModel(
                    href = it.attr(ATTR_HREF),
                    name = it.text(),
                    description = ""
                )
            }
        } ?: emptyList()

        val pairings: List<PairingModel> = fanficInlineInfo.find { element ->
            element.select("dt:contains(Пэйринг и персонажи:)").isNotEmpty()
        }?.let { element ->
            element.select("dd a").map { a ->
                val character = a.text()
                val href = a
                    .attr(ATTR_HREF)
                    .let { URLDecoder.decode(it, "UTF-8") }

                val isHighlighted = a.className().contains("pairing-highlight")

                PairingModel(
                    character = character,
                    href = href,
                    isHighlighted = isHighlighted
                )
            }
        } ?: emptyList()

        val updateDate = fanficInlineInfo.firstOrNull {
            it.text().contains("Дата обновления:") ||
                    it.text().contains("Дата завершения:") ||
                    it.text().contains("Дата создания:")
        }
            ?.select("dd")
            ?.text()
            ?: ""

        val size = fanficInlineInfo.find { element ->
            element.select("dt:contains(Размер:)").isNotEmpty()
        }
            ?.select("dd")
            ?.text()
            ?: ""

        val tagsElements = data
            .select(".tags")
            .select("a")

        val tags = tagsElements.map {
            FanficTag(
                name = it.text(),
                isAdult = it.outerHtml().contains("tag-adult"),
                href = it.attr(ATTR_HREF)
            )
        }


        val description = data.select(".fanfic-description")
            .wholeText
            .trim(' ', '\n')

        val coverUrl = data
            .select(".side-Section")
            .select("source")
            .attr("srcset")
            .let(::CoverUrl)

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
            fandoms = fandom,
            pairings = pairings,
            updateDate = updateDate,
            size = size,
            tags = tags,
            description = description,
            coverUrl = coverUrl,
            readInfo = readInfo
        )
    }
}
