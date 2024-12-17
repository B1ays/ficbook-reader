package ru.blays.ficbook.api.parsers

import kotlinx.coroutines.coroutineScope
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

internal suspend fun checkPageButtonsExists(document: Document): PageButtonsStatus = coroutineScope {
    val pagination = document.select("nav.pagination")

    if(pagination.isEmpty()) {
        return@coroutineScope PageButtonsStatus(
            hasNext = false,
            hasPrevious = false
        )
    }

    val arrows = pagination.select("[class*=arrow]")

    if(arrows.size < 2) {
        return@coroutineScope PageButtonsStatus(
            hasNext = false,
            hasPrevious = false
        )
    }

    val backwardButtonActive = !arrows[0].className().contains("disabled")
    val forwardButtonActive = !arrows[1].className().contains("disabled")

    return@coroutineScope PageButtonsStatus(
        hasNext = forwardButtonActive,
        hasPrevious = backwardButtonActive
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
