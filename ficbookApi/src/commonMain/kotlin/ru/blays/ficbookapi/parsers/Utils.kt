package ru.blays.ficbookapi.parsers

import kotlinx.coroutines.coroutineScope
import org.jsoup.nodes.Document
import org.jsoup.select.Evaluator

internal suspend fun checkPageButtonsExists(document: Document): PageButtonsStatus = coroutineScope {
    val pageNav = document.select(
        Evaluator.Class("pagenav my-15")
    )
    val hasPrevious = if(pageNav.isNotEmpty()) {
        val backwardButton = pageNav.select("[class=\"page-arrow page-arrow-prev\"]")
        !backwardButton.hasClass("disabled")
    } else {
        false
    }
    val hasNext =if(pageNav.isNotEmpty()) {
        val forwardButton = pageNav.select("[class=\"page-arrow page-arrow-next\"]")
        !forwardButton.hasClass("disabled")
    } else {
        false
    }
    return@coroutineScope PageButtonsStatus(
        hasNext = hasNext,
        hasPrevious = hasPrevious
    )
}

internal data class PageButtonsStatus(
    val hasNext: Boolean,
    val hasPrevious: Boolean
)