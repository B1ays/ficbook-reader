package ru.blays.ficbook.api.UrlProcessor

import ru.blays.ficbook.api.ficbookExtensions.ficbookUrl
import ru.blays.ficbook.api.okHttpDsl.href


fun getUrlForHref(href: String): String = ficbookUrl {
    href(href)
}.toString()