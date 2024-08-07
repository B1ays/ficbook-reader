package ru.blays.ficbook.api.parsers

import kotlinx.coroutines.coroutineScope
import org.jsoup.nodes.Document
import org.jsoup.nodes.Entities
import org.jsoup.select.Elements
import org.jsoup.select.Evaluator
import ru.blays.ficbook.api.ATTR_HREF
import ru.blays.ficbook.api.ATTR_SRC
import ru.blays.ficbook.api.dataModels.*
import ru.blays.ficbook.api.json
import ru.blays.ficbook.api.notNumberRegex
import java.net.URLDecoder

internal class FanficPageParser {
    private val chapterTextParser = ChapterParser()

    suspend fun parse(data: Document): FanficPageModel = coroutineScope {
        val outputSettings: Document.OutputSettings = Document.OutputSettings()
        outputSettings.prettyPrint(false)
        outputSettings.escapeMode(Entities.EscapeMode.extended)
        data.outputSettings(outputSettings)

        val fanficMainInfo = data.select(".fanfic-main-info").first()
        val bottomAction = data.select(
            Evaluator.Class("mb-15 text-center")
        )
        val mb5 = data.select(Evaluator.Class("mb-5"))

        val id = data.select("[data-fanfic-id]")
            .firstOrNull()
            ?.attr("data-fanfic-id")
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
            ?.hasClass("fanfic-hat-premium-notice")
            ?: false

        val name = fanficMainInfo?.run {
            val name = select("[itemprop='name']").text()
            val headline = select("[itemprop='headline']").text()
            return@run name.ifEmpty { headline }
        } ?: ""

        val fandoms: List<FandomModel> = fanficMainInfo
            ?.select(".mb-10")
            ?.run {
                forEach { element ->
                    if (element.html().contains("ic_book")) {
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
            ?: emptyList()

        val authors: List<FanficAuthorModel> = data
            .select(".hat-creator-container")
            .map { element ->
                val avatar = element.select("img").attr(ATTR_SRC)
                val creatorInfo = element.select(".creator-username")
                val role = element.select(Evaluator.Class("small-text text-muted")).text()
                val user = UserModel(
                    name = creatorInfo.text(),
                    href = creatorInfo.attr(ATTR_HREF),
                    avatarUrl = avatar
                )
                FanficAuthorModel(
                    user = user,
                    role = role
                )
            }

        val pairings = data.select(
            Evaluator.Class("description word-break")
        )
            .select("a")
            .filter { element ->
                element.className().contains("pairing-link")
            }
            .map {
                val character = it.text()
                val href = it.attr(ATTR_HREF).let { url ->
                    URLDecoder.decode(url, "UTF-8")
                }

                val isHighlighted = it.className().contains("pairing-highlight")
                PairingModel(
                    href = href,
                    character = character,
                    isHighlighted = isHighlighted
                )
            }

        val description = mb5
            .select("div:contains(Описание:)")
            .wholeText
            .trim(' ', '\n')
            .replace(
                regex = Regex("Описание:\\s*"),
                replacement = ""
            )

        val dedication = mb5
            .select("div:contains(Посвящение:)")
            .takeIf(Elements::isNotEmpty)
            ?.wholeText
            ?.trim(' ', '\n')
            ?.replace(
                regex = Regex("Посвящение:\\s*"),
                replacement = ""
            )

        val authorComment = mb5
            .select("div:contains(Примечания:)")
            .takeIf(Elements::isNotEmpty)
            ?.wholeText
            ?.trim(' ', '\n')
            ?.replace(
                regex = Regex("Примечания:\\s*"),
                replacement = ""
            )

        val publicationRules = mb5
            .select("div:contains(Публикация на других ресурсах:)")
            .wholeText
            .trim(' ', '\n')
            .replace(
                regex = Regex("Публикация на других ресурсах:\\s*"),
                replacement = ""
            )

        val coverUrl = data
            .select(".fanfic-hat")
            .select("fanfic-cover")
            .attr("src-desktop")
            .let(::CoverUrl)

        val tags = mb5
            .select(".tags")
            .select("a")

        val genres = tags.map {
            FanficTag(
                name = it.text(),
                isAdult = it.outerHtml().contains("tag-adult")
            )
        }

        val parts = data
            .select(Evaluator.Class("article mb-15"))
            .select(".part")

        val fanficChapters = if(parts.isNotEmpty()) {
            val chapters = parts.map { element ->
                val partInfo = element.select(
                    Evaluator.Class("part-info")
                )

                val href = element
                    .select("a")
                    .attr(ATTR_HREF)
                val chapterID = href
                    .substringAfterLast('/')
                    .substringBefore('#')

                val chapterName = element
                    .select(Evaluator.Class("part-title word-break"))
                    .text()

                val date = partInfo.select("span").text()

                val commentsCount = partInfo.select("a")
                    .text()
                    .replace(
                        regex = notNumberRegex,
                        replacement = ""
                    )
                    .toIntOrNull()
                    ?: 0

                FanficChapter.SeparateChaptersModel.Chapter(
                    chapterID = chapterID,
                    href = href,
                    name = chapterName,
                    date = date,
                    commentsCount = commentsCount
                )
            }
            FanficChapter.SeparateChaptersModel(
                chapters = chapters,
                chaptersCount = chapters.size
            )
        } else {
            val chapterID = data.select(".form-comments input").attr("value")

            val date = data
                .select(".part-date")
                .text()

            val text = chapterTextParser.parseText(data)

            FanficChapter.SingleChapterModel(
                chapterID = chapterID,
                date = date,
                text = text
            )
        }

        val liked = bottomAction
            .select("span")
            .run {
                firstOrNull {
                    it.outerHtml().contains("js-like")
                }.let {
                    it?.outerHtml()?.contains("btn-success") == true
                }
            }

        val subscribed = bottomAction
            .select("fanfic-follow-button")
            .attr(":is-followed").toBoolean()

        val inCollectionsCount = bottomAction
            .select("fanfic-collections-modal")
            .attr(":initial-count-of-fanfic-collection")
            .toIntOrNull()
            ?: 0


        val pagesCount: Int = mb5.run {
            val sizeElements = select("div:contains(Размер:)")
            if (sizeElements.isNotEmpty()) {
                val sizeInfoParts = sizeElements.text().split(", ")
                when (sizeInfoParts.size) {
                    3 -> {
                        val rawString = sizeInfoParts[1]
                        return@run rawString.replace(
                            regex = notNumberRegex,
                            replacement = ""
                        )
                        .toIntOrNull()
                        ?: 0
                    }
                    2 -> {
                        val rawString = sizeInfoParts[0]
                        return@run rawString.replace(
                            regex = notNumberRegex,
                            replacement = ""
                        )
                        .toIntOrNull()
                        ?: 0
                    }
                    else -> 0
                }
            } else 0
        }

        val rewardAnswer = data
            .select(
                Evaluator.Class("fanfic-reward-container rounded-block")
            )
            .select("fanfic-reward-list")
            .attr(":initial-fic-rewards-list")

        val rewardsSerialized: List<RewardResponseItem> = try {
            json.decodeFromString(rewardAnswer)
        } catch (e: Exception) {
            emptyList()
        }

        val rewards = rewardsSerialized.map {
            RewardModel(
                message = it.userText,
                fromUser = it.username,
                awardDate = it.dateAdded
            )
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
            authors = authors,
            fandoms = fandoms,
            pairings = pairings,
            coverUrl = coverUrl,
            tags = genres,
            description = description,
            dedication = dedication,
            authorComment = authorComment,
            publicationRules = publicationRules,
            subscribersCount = 0,
            commentCount = 0,
            liked = liked,
            subscribed = subscribed,
            inCollectionsCount = inCollectionsCount,
            chapters = fanficChapters,
            rewards = rewards,
            pagesCount = pagesCount
        )
    }
}