package ru.blays.ficbookapi.UrlProcessor

import ru.blays.ficbookapi.ficbookExtensions.ficbookUrl
import ru.blays.ficbookapi.okHttpDsl.href

fun getUrlForHref(href: String): String = ficbookUrl {
    href(href)
}.toString()