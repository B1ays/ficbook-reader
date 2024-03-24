package ru.blays.ficbook.api.parsers

import kotlinx.coroutines.coroutineScope
import org.jsoup.nodes.Document
import org.jsoup.select.Evaluator

internal class ChapterParser {
    val outputSettings = Document.OutputSettings().apply {
        prettyPrint(false)
    }

    suspend fun parseText(data: Document): String = coroutineScope {
        val topText = data.select(
            Evaluator.Class("part-comment-top mx-10 mx-xs-5")
        ).wholeText
        val text = data.select(
            Evaluator.Class("js-part-text part_text clearfix js-public-beta-text js-bookmark-area")
        ).wholeText
        val bottomText = data.select(
            Evaluator.Class("part-comment-bottom mx-10 mx-xs-5")
        ).wholeText
        return@coroutineScope "$topText\n$text\n$bottomText"
    }

    suspend fun parseHtml(data: Document): String = coroutineScope {
        data.outputSettings(outputSettings)

        data.outputSettings()
        val topHtml = data.select(
            Evaluator.Class("part-comment-top mx-10 mx-xs-5")
        )
        .html()
        .replace("\n", "<br/>")
        val textHtml = data.select(
            Evaluator.Class("js-part-text part_text clearfix js-public-beta-text js-bookmark-area")
        )
        .html()
        .replace("\n", "<br/>")
        val bottomHtml = data.select(
            Evaluator.Class("part-comment-bottom mx-10 mx-xs-5")
        )
        .html()
            .replace("\n", "<br/>")
        return@coroutineScope "$topHtml\n$textHtml\n$bottomHtml"
    }
}