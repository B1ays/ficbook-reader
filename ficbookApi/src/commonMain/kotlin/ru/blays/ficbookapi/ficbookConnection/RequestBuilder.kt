package ru.blays.ficbookapi.ficbookConnection

import okhttp3.HttpUrl
import okhttp3.Request
import ru.blays.ficbookapi.FICBOOK_HOST
import ru.blays.ficbookapi.QUERY_PAGE

internal fun buildFicbookURL(block: HttpUrl.Builder.() -> Unit): HttpUrl {
    val builder = HttpUrl.Builder()
    builder.scheme("https")
    builder.host(FICBOOK_HOST)
    block(builder)
    return builder.build()
}

internal fun buildFicbookRequest(block: Request.Builder.() -> Unit): Request {
    val builder = Request.Builder()

    block(builder)
    return builder.build()
}

internal fun HttpUrl.Builder.page(page: Int): HttpUrl.Builder {
    return addQueryParameter(QUERY_PAGE, page.toString())
}

internal fun HttpUrl.Builder.href(href: String): HttpUrl.Builder {
    val segments = if(href.startsWith("/")) {
        href.removePrefix("/")
    } else href
    return addPathSegments(segments)
}