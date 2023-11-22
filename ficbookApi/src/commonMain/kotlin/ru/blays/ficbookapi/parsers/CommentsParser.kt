package ru.blays.ficbookapi.parsers

import kotlinx.coroutines.flow.StateFlow
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.jsoup.select.Evaluator
import ru.blays.ficbookapi.dataModels.*

class CommentListParser: IDataParser<Document, Elements> {
    override suspend fun parse(data: Document): Elements {
        return data.select(".comment-container")
    }

    override fun parseSynchronously(data: Document): StateFlow<Elements?> {
        TODO("Not yet implemented")
    }
}

class CommentParser: IDataParser<Element, CommentModel> {
    override suspend fun parse(data: Element): CommentModel {
        val avatarUrl = data.select(".comment-avatar img").attr("src")
        val (href, userName) = data.select(".author")
            .select("a")
            .let {
                it.attr("href") to it.text()
            }
        val commentMessage = data.select(
            Evaluator.Class("comment_message urlize js-comment-message")
        ).first()
        val date = data.select("time").text()
        val likes = data.select("like-button")
            .attr(":like-count")
            .toIntOrNull()
            ?: 0

        val forFanfic: FanficShortcut? = data.select(".comment_link_to_fic")
            .first()
            ?.select("a[class=\"btn btn-link btn-sm px-0 word-break\"]")
            ?.let {
                FanficShortcut(
                    name = it.text(),
                    href = it.attr("href").substringBeforeLast('/')
                )
            }

        val blocks = if(commentMessage != null) {
            val wholeText = commentMessage.wholeText()
            parseBlocks(wholeText)
        } else {
            emptyList()
        }

        return CommentModel(
            user = UserModel(
                name = userName,
                href = href,
                avatarUrl = avatarUrl
            ),
            date = date,
            blocks = blocks,
            likes = likes,
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

    private suspend fun parseStringBlocks(lines: List<String>): MutableList<List<String>> {
        val blocks = mutableListOf<List<String>>()
        var blockStartIndex = 0
        var blockEndIndex = 0
        var blockEnded = false
        lines.forEachIndexed { index, line ->
            if(line.startsWith('>')) {
                if (blockEnded) {
                    blocks += lines.slice(blockStartIndex..blockEndIndex)
                    blockStartIndex = index
                    blockEndIndex = index
                    blockEnded = false
                }
            } else {
                blockEndIndex = index
                blockEnded = true
            }
        }
        blocks += lines.slice(blockStartIndex..lines.lastIndex)

        return blocks
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
        if(linesOnLevel.isEmpty()) return null
        var userNameIndex: Short = NO_USER_NAME
        val userName = linesOnLevel.firstOrNull { it.startsWith("$prefix**") }
            ?.also {
                userNameIndex = linesOnLevel.indexOf(it).toShort()
            }
            ?.substringAfter("**")
            ?.substringBefore("**")
            ?: ""

        val text = linesOnLevel.run {
            if(userNameIndex == NO_USER_NAME) {
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

    suspend fun blockToText(blockModel: CommentBlockModel): String {
        val stringBuilder = StringBuilder()
        blockModel.quote?.let {
            stringBuilder.append(quoteToText(it))
        }
        stringBuilder.append(blockModel.text)
        return stringBuilder.toString()
    }

    private suspend fun quoteToText(quoteModel: QuoteModel, level: Int = 1): String {
        val stringBuilder = StringBuilder()
        val prefix = ">".repeat(level)
        val text = quoteModel.text
        val userName = quoteModel.userName
        stringBuilder.appendLine("$prefix**$userName**")
        quoteModel.quote?.let {
            stringBuilder.appendLine(quoteToText(it, level + 1))
        }
        val textLines = text.lines()
        if(textLines.isNotEmpty()) {
            textLines.forEach { line ->
                stringBuilder.appendLine("$prefix$line")
            }
        }

        return stringBuilder.toString()
    }

    @Suppress("PrivatePropertyName")
    private val NO_USER_NAME: Short = -1

    override fun parseSynchronously(data: Element): StateFlow<CommentModel?> {
        TODO("Not yet implemented")
    }
}