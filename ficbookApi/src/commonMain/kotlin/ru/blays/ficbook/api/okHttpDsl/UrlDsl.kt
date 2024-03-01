package ru.blays.ficbook.api.okHttpDsl

import okhttp3.HttpUrl
import ru.blays.ficbook.api.QUERY_PAGE

fun httpUrl(block: HttpUrl.Builder.() -> Unit): HttpUrl {
    val builder = HttpUrl.Builder()
    builder.block()
    return builder.build()
}

fun HttpUrl.Builder.href(href: String) {
    val clearedHref = href.substringBefore('#').trim(' ', '/')
    addPathSegments(clearedHref)
}

fun HttpUrl.Builder.page(page: Int) {
    addQueryParameter(QUERY_PAGE, page.toString())
}

fun HttpUrl.Builder.queryParams(list: List<Pair<String, String>>) {
    list.forEach { (name, value) ->
        addQueryParameter(name, value)
    }
}