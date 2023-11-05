package ru.blays.ficbookapi.parsers

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.apache.commons.text.StringEscapeUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Entities
import org.jsoup.select.Evaluator
import ru.blays.ficbookapi.dataModels.*
import java.net.URLDecoder

internal class FanficPageParser: IDataParser<String, FanficPageModel> {
    override suspend fun parse(data: String): FanficPageModel = coroutineScope {
        val document = Jsoup.parse(data)
        val outputSettings: Document.OutputSettings = Document.OutputSettings()
        outputSettings.prettyPrint(false)
        outputSettings.escapeMode(Entities.EscapeMode.extended)
        document.outputSettings(outputSettings)

        val fanficMainInfo = document.select(".fanfic-main-info").first()
        val bottomAction = document.select(
            Evaluator.Class("mb-15 text-center")
        )
        val mb5 = document.select(Evaluator.Class("mb-5"))
        val header = document.head()

        val id = header
            .select("[property=og:url]")
            .attr("content")
            .split("/")
            .lastOrNull()
            ?: ""

        val likes = fanficMainInfo
            ?.select(
                Evaluator.Class("badge-with-icon badge-secondary badge-like")
            )
            ?.select(".badge-text")
            ?.text()
            ?.toIntOrNull()
            ?: 0

        val trophies = fanficMainInfo?.select(
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
            ?.hasClass("fanfic-hat-premium-notice")
            ?: false

        val name = fanficMainInfo?.run {
            val name = select("[itemprop='name']").text()
            val headline = select("[itemprop='headline']").text()
            return@run name.ifEmpty { headline }
        } ?: ""

        val fandom: List<FandomModel> = fanficMainInfo
            ?.select(".mb-10")
            ?.run {
                forEach { element ->
                    if(element.html().contains("ic_book")) {
                        val a = element.select("a")
                        return@run a.map {
                            FandomModel(
                                href = it.attr("href"),
                                name = it.text(),
                                description = ""
                            )
                        }
                    }
                }
                return@run emptyList()
            }
            ?: emptyList()


        val author: List<UserModel> = document
            .select(".hat-creator-container")
            .map { element ->
                val avatar = element.select("img").attr("src")
                val creatorInfo = element.select(".creator-username")
                UserModel(
                    name = creatorInfo.text(),
                    href = creatorInfo.attr("href"),
                    avatarUrl = avatar
                )
            }

        val pairings = document.select(Evaluator.Class("description word-break"))
            .select("a")
            .filter { element ->
                element.className().contains("pairing-link")
            }
            .map {
                val character = it.text()
                val href = it
                    .attr("href")
                    .let { url -> URLDecoder.decode(url, "UTF-8") }

                val isHighlighted = it.className().contains("pairing-highlight")
                PairingModel(
                    href = href,
                    character = character,
                    isHighlighted = isHighlighted
                )
            }

        val description = mb5
            .select("div:contains(Описание:)")
            .text()
            .removePrefix("Описание: ")

        val coverUrl = document
            .select(".fanfic-hat")
            .select("fanfic-cover")
            .attr("src-desktop")
            .let { CoverUrl(it) }


        val genres = mutableListOf<FanficTag>().apply {
            val tags = mb5
                .select(".tags")
                .select("a")
            tags.forEach {
                add(
                    FanficTag(
                        name = it.text(),
                        isAdult = it.outerHtml().contains("tag-adult")
                    )
                )
            }
        }

        val parts = document
            .select(
                Evaluator.Class("article mb-15")
            )
            .select(".part")

        val chaptersList = mutableListOf<FanficChapter>()

        if(parts.isNotEmpty()) {
            parts.forEach { part ->
                val href = part
                    .select("a")
                    .attr("href")

                val chapterName = part
                    .select(
                        Evaluator.Class("part-title word-break")
                    ).text()

                val partInfo = part.select(
                    Evaluator.Class("part-info text-muted")
                )
                val date = partInfo
                    .select("span")
                    .text()
                val (commentsCount, commentsHref) = partInfo
                    .select("a")
                    .run {
                        (
                            text().removePrefix("Отзывы: ")
                                .toIntOrNull()
                                ?: 0
                        ) to attr("href")
                    }

                chaptersList += FanficChapter.SeparateChapterModel(
                    href = href,
                    name = chapterName,
                    date = date,
                    commentsCount = commentsCount,
                    commentsHref = commentsHref
                )
            }
        } else {
            val date = document
                .select(".part-date")
                .text()

            val (commentsCount, commentsHref) = document
                .select(
                    Evaluator.Class("btn btn-primary btn-with-description")
                )
                .firstOrNull {
                    it.select(".description")
                        .text()
                        .contains("Отзывы")
                }
                ?.run {
                    (
                        select("span")
                            .text()
                            .trim()
                            .toIntOrNull()
                            ?: 0
                    ) to attr("href")
                }
                ?: (0 to "")

            val chapterTextParser = ChapterTextParser()

            val textElements = document.select(
                Evaluator.Class("js-part-text part_text clearfix js-public-beta-text js-bookmark-area")
            )
            val text = chapterTextParser.parse(textElements)

            chaptersList += FanficChapter.SingleChapterModel(
                date,
                commentsCount,
                commentsHref,
                text
            )
        }

        val liked = bottomAction
            .select("span")
            .run {
                val first = firstOrNull {
                    it.outerHtml().contains("js-like")
                }
                first?.outerHtml()?.contains("btn-success") == true
            }

        val subscribed = bottomAction
            .select("fanfic-follow-button")
            .run {
                val isFollowed = attr(":is-followed")
                isFollowed == "true"
            }

        val pagesCount: Int = mb5.run {
            val sizeElements = select("div:contains(Размер:)")
            if (sizeElements.isNotEmpty()) {
                val sizeInfoParts = sizeElements.text().split(", ")
                when (sizeInfoParts.size) {
                    3 -> {
                        val rawString = sizeInfoParts[1]
                        return@run rawString
                            .replace(
                                regex = "[^0-9]".toRegex(),
                                replacement = ""
                            )
                            .toIntOrNull()
                            ?: 0
                    }
                    2 -> {
                        val rawString = sizeInfoParts[0]
                        return@run rawString
                            .replace(
                                regex = "[^0-9]".toRegex(),
                                replacement = ""
                            )
                            .toIntOrNull()
                            ?: 0
                    }
                    else -> {
                        0
                    }
                }
            } else {
                0
            }
        }

        val rewardsList = mutableListOf<RewardModel>()

        val rewardAnswer = document
            .select(
                Evaluator.Class("fanfic-reward-container rounded-block")
            )
            .select("fanfic-reward-list")
            .attr(":initial-fic-rewards-list")
            .run { StringEscapeUtils.unescapeJava(this) }

        val rewardsSerialized = try {
            Json.decodeFromString<List<RewardResponseItem>>(rewardAnswer)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }

        rewardsSerialized.forEach {
            val message = it.userText
            val fromUser = it.username
            val date = it.dateAdded
            rewardsList += RewardModel(message, fromUser, date)
        }

        return@coroutineScope FanficPageModel(
            id = id,
            name = name,
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
            pairings = pairings,
            coverUrl = coverUrl,
            tags = genres,
            description = description,
            subscribersCount = 0,
            commentCount = 0,
            liked = liked,
            subscribed = subscribed,
            inCollectionsCount = 0,
            chapters = chaptersList,
            rewards = rewardsList,
            pagesCount = pagesCount
        )
    }

    override fun parseSynchronously(data: String): StateFlow<FanficPageModel?> {
        val resultFlow = MutableStateFlow<FanficPageModel?>(null)
        launch {
            resultFlow.value = parse(data)
        }
        return resultFlow
    }

}