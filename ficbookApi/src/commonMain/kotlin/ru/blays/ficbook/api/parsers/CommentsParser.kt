package ru.blays.ficbook.api.parsers

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.jsoup.select.Evaluator
import ru.blays.ficbook.api.ATTR_HREF
import ru.blays.ficbook.api.ATTR_SRC
import ru.blays.ficbook.api.dataModels.*
import ru.blays.ficbook.api.json

internal class CommentListParser {
    suspend fun parse(data: Document): Elements {
        return data.select("article.comment-container")
    }
}

class CommentParser {
    suspend fun parse(data: Element): CommentModel {
        val commentID = data.select(
            Evaluator.Class("mt-5 js quote btn btn-sm btn-link")
        ).attr("data-comment_id")

        val avatarUrl = data.select(".comment-avatar img").attr(ATTR_SRC)
        val (href, userName) = data.select(".author")
            .select("a")
            .let { it.attr(ATTR_HREF) to it.text() }

        val isOwnComment = data.select(
            Evaluator.Class("btn btn-link delete js_delete_comment")
        ).isNotEmpty()

        val likeButton = data.select("comment-like").first()
        val metadata = likeButton?.attr(":comment")

        val metadataModel: CommentMetadata? = metadata?.let(json::decodeFromString)

        val likes = metadataModel?.likeCnt ?: 0

        val isLiked: Boolean = metadataModel?.liked ?: false

        val likedBy: List<FanficAuthorModel> = metadataModel?.likeBadges?.map { badge ->
            FanficAuthorModel(
                user = UserModel(
                    name = badge.username,
                    href = "authors/${badge.id}",
                    avatarUrl = badge.avatarUrl
                ),
                role = badge.type
            )
        } ?: emptyList()

        val commentMessage = data.select(
            Evaluator.Class("comment_message urlize js-comment-message")
        ).first()

        val date = data.select("time").text()

        val forFanfic: FanficShortcut? = data.select(".comment_link_to_fic")
            .first()
            ?.select("a[class=\"btn btn-link btn-sm px-0 word-break\"]")
            ?.let {
                FanficShortcut(
                    name = it.text(),
                    href = it.attr(ATTR_HREF).substringBeforeLast('/')
                )
            }

        val blocks = if (commentMessage != null) {
            val wholeText = commentMessage.wholeText()
            parseBlocks(wholeText)
        } else {
            emptyList()
        }

        return CommentModel(
            commentID = commentID,
            user = UserModel(
                name = userName,
                href = href,
                avatarUrl = avatarUrl
            ),
            isOwnComment = isOwnComment,
            isLiked = isLiked,
            date = date,
            blocks = blocks,
            likes = likes,
            likedBy = likedBy,
            forFanfic = forFanfic
        )
    }

    suspend fun parseBlocks(text: String): List<CommentBlockModel> {
        val lines = text.lines()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .filterNot { it.startsWith(">\n") || it == ">" }

        val stringBlocks = parseStringBlocks(lines)

        return stringBlocks.map { parseBlock(it) }
    }

    private suspend fun parseStringBlocks(lines: List<String>): List<List<String>> {
        var blockStartIndex = 0
        var blockEndIndex = 0
        var blockEnded = false
        val blocks = lines.mapIndexedNotNull { index, line ->
            if (line.startsWith('>')) {
                if (blockEnded) {
                    val block = lines.slice(blockStartIndex..blockEndIndex)
                    blockStartIndex = index
                    blockEndIndex = index
                    blockEnded = false
                    block
                } else {
                    null
                }
            } else {
                blockEndIndex = index
                blockEnded = true
                null
            }
        }

        val lastBlock = lines.slice(blockStartIndex..lines.lastIndex)

        return blocks + listOf(lastBlock)
    }

    private suspend fun parseBlock(block: List<String>): CommentBlockModel {
        val text = block.filterNot { it.startsWith('>') }
            .joinToString("\n")
        val quote = quoteParser(block)
        return CommentBlockModel(
            quote = quote,
            text = text
        )
    }

    private suspend fun quoteParser(block: List<String>, level: Int = 1): QuoteModel? {
        val prefix = ">".repeat(level)
        val linesOnLevel = block.filter { it.matches("(?<!>)>{$level}[^>]+".toRegex()) }
        if (linesOnLevel.isEmpty()) return null
        var userNameIndex: Short = NO_USER_NAME
        val userName = linesOnLevel.firstOrNull { it.startsWith("$prefix**") }
            ?.also {
                userNameIndex = linesOnLevel.indexOf(it).toShort()
            }
            ?.substringAfter("**")
            ?.substringBefore("**")
            ?: ""

        val text = linesOnLevel.run {
            if (userNameIndex == NO_USER_NAME) {
                linesOnLevel.joinToString("\n") { it.trim('>') }
            } else {
                linesOnLevel.slice(
                    (userNameIndex + 1)..(linesOnLevel.lastIndex)
                )
                .joinToString("\n") {
                    it.trim('>')
                }
            }
        }

        val quotes = quoteParser(block, level + 1)

        return QuoteModel(
            quote = quotes,
            userName = userName,
            text = text
        )
    }

    suspend fun blockToText(
        blockModel: CommentBlockModel,
        initialLevel: Int = 1
    ): String {
        val stringBuilder = StringBuilder()
        blockModel.quote?.let {
            stringBuilder.append(
                quoteToText(
                    quoteModel = it,
                    level = initialLevel
                )
            )
        }
        stringBuilder.append(blockModel.text)
        return stringBuilder.toString()
    }

    private suspend fun quoteToText(
        quoteModel: QuoteModel,
        level: Int = 1
    ): String {
        val stringBuilder = StringBuilder()
        val prefix = ">".repeat(level)
        val text = quoteModel.text
        val userName = quoteModel.userName
        if (userName.isNotEmpty()) {
            stringBuilder.appendLine("$prefix**$userName**")
        }
        quoteModel.quote?.let {
            stringBuilder.append(quoteToText(it, level + 1))
        }
        val textLines = text.lines()
        if (textLines.isNotEmpty()) {
            textLines.forEach { line ->
                stringBuilder.appendLine("$prefix$line")
                stringBuilder.appendLine('>')
            }
        }

        return stringBuilder.toString()
    }

    @Suppress("PrivatePropertyName")
    private val NO_USER_NAME: Short = -1
}