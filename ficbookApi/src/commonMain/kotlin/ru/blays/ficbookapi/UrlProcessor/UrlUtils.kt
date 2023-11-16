package ru.blays.ficbookapi.UrlProcessor

import ru.blays.ficbookapi.ficbookConnection.buildFicbookURL
import ru.blays.ficbookapi.ficbookConnection.href

fun getUrlForHref(href: String): String =
    buildFicbookURL {
        href(href)
    }.toString()