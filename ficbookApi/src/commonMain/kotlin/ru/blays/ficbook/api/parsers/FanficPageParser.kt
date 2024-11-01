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

    private val outputSettings: Document.OutputSettings = Document.OutputSettings().apply {
        prettyPrint(false)
        escapeMode(Entities.EscapeMode.extended)
    }

    suspend fun parse(data: Document): FanficPageModel = coroutineScope {
        data.outputSettings(outputSettings)

        val chapterInfo = data.select(".chapter-info").first()

        val bottomAction = data.select(".hat-actions-container")

        val mb10 = data.select(Evaluator.Class("mb-10"))

        val id = data.select("[data-fanfic-id]")
            .firstOrNull()
            ?.attr("data-fanfic-id")
            ?: ""

        val likes = chapterInfo
            ?.select(Evaluator.Class("badge-with-icon badge-secondary badge-like"))
            ?.select(".badge-text")
            ?.text()
            ?.toIntOrNull()
            ?: 0

        val rating = chapterInfo
            ?.select("div[class*='badge-rating']")
            ?.let { FanficRating.getForName(it.text()) }
            ?: FanficRating.UNKNOWN

        val direction = chapterInfo
            ?.select("div[class*='direction']")
            ?.let { FanficDirection.getForName(it.text()) }
            ?: FanficDirection.UNKNOWN

        val status = chapterInfo
            ?.select("div[class*='status']")
            ?.let { FanficCompletionStatus.getForName(it.text()) }
            ?: FanficCompletionStatus.UNKNOWN

        val isHot = chapterInfo
            ?.select(".fanfic-hat-premium-notice")
            ?.isNotEmpty()
            ?: false

        val name = chapterInfo
            ?.run {
                val name = select("[itemprop='name']").text()
                name.ifEmpty { select("[itemprop='headline']").text() }
            }
            ?: ""

        val fandoms: List<FandomModel> = chapterInfo
            ?.select("div")
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

        val pairings = data
            .select(Evaluator.Class("description word-break"))
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

        val description = mb10
            .select("div:contains(Описание:)")
            .wholeText
            .trim(' ', '\n')
            .replace(
                regex = Regex("Описание:\\s*"),
                replacement = ""
            )

        val dedication = mb10
            .select("div:contains(Посвящение:)")
            .takeIf(Elements::isNotEmpty)
            ?.wholeText
            ?.trim(' ', '\n')
            ?.replace(
                regex = Regex("Посвящение:\\s*"),
                replacement = ""
            )

        val authorComment = mb10
            .select("div:contains(Примечания:)")
            .takeIf(Elements::isNotEmpty)
            ?.wholeText
            ?.trim(' ', '\n')
            ?.replace(
                regex = Regex("Примечания:\\s*"),
                replacement = ""
            )

        val publicationRules = mb10
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

        val tags = mb10.select(".tags > a")

        val genres = tags.map {
            FanficTag(
                name = it.text(),
                isAdult = it.outerHtml().contains("tag-adult")
            )
        }

        val parts = data
            .select("article.article")
            .select(".part")

        val fanficChapters = if(parts.isNotEmpty()) {
            val chapters = parts.map { element ->
                val partInfo = element.select(".part-info")

                val href = element
                    .select("a")
                    .attr(ATTR_HREF)
                val chapterID = href
                    .substringAfterLast('/')
                    .substringBefore('#')

                val chapterName = element
                    .select("h3")
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

            val topHtml = data
                .select(Evaluator.Class("part-comment-top mx-10 mx-xs-5"))
                .html()
                .replace("\n", "<br/>")

            val textHtml = data
                .select(Evaluator.Class("js-part-text part_text clearfix js-public-beta-text js-bookmark-area"))
                .html()
                .replace("\n", "<br/>")

            val bottomHtml = data
                .select(Evaluator.Class("part-comment-bottom mx-10 mx-xs-5"))
                .html()
                .replace("\n", "<br/>")

            val html = "$topHtml\n$textHtml\n$bottomHtml"

            FanficChapter.SingleChapterModel(
                chapterID = chapterID,
                date = date,
                text = html
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

        val pagesCount: Int = mb10.run {
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
            .select("fanfic-reward-list")
            .attr(":initial-fic-rewards-list")

        val rewardsRaw: List<RewardResponseItem> = if (rewardAnswer.isNotEmpty()) {
            try {
                json.decodeFromString(rewardAnswer)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        val rewards = rewardsRaw.map {
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
                trophies = 0
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