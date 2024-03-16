package ru.blays.ficbook.api.parsers

import kotlinx.coroutines.coroutineScope
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.jsoup.select.Evaluator

internal suspend fun checkPageButtonsExists(document: Document): PageButtonsStatus = coroutineScope {
    val pageNav = document.select(
        Evaluator.Class("pagenav my-15")
    )

    if (pageNav.isEmpty()) {
        return@coroutineScope PageButtonsStatus(
            hasNext = false,
            hasPrevious = false
        )
    }

    val backwardButton = pageNav.select("[class=\"page-arrow page-arrow-prev\"]")
    val forwardButton = pageNav.select("[class=\"page-arrow page-arrow-next\"]")

    return@coroutineScope PageButtonsStatus(
        hasNext = !backwardButton.hasClass("disabled"),
        hasPrevious = !forwardButton.hasClass("disabled")
    )
}

internal data class PageButtonsStatus(
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

internal val Elements.wholeText: String
    get() = fold("") { acc, element ->
        acc + element.wholeText()
    }
