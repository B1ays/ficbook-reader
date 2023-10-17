package ru.blays.ficbookapi.ficbookConnection

import okhttp3.HttpUrl
import okhttp3.Request
import ru.blays.ficbookapi.FICBOOK_HOST
import ru.blays.ficbookapi.HEADER_COOKIE
import ru.blays.ficbookapi.dataModels.CookieModel

internal fun buildFicbookURL(block: HttpUrl.Builder.() -> Unit): HttpUrl {
    val builder = HttpUrl.Builder()
    builder.scheme("https")
    builder.host(FICBOOK_HOST)
    block(builder)
    return builder.build()
}

internal fun buildFicbookRequest(cookies: List<CookieModel>? = null, block: Request.Builder.() -> Unit): Request {
    val builder = Request.Builder()
    if(!cookies.isNullOrEmpty()) {
        val cookiesString = cookies.joinToString("; ") { it.toHttpFormat() }
        builder.addHeader(HEADER_COOKIE, cookiesString)
    }
    block(builder)
    return builder.build()
}

internal fun HttpUrl.Builder.page(page: Int): HttpUrl.Builder {
    return addQueryParameter("p", page.toString())
}

internal fun HttpUrl.Builder.href(href: String): HttpUrl.Builder {
    val segments = if(href.startsWith("/")) {
        href.removePrefix("/")
    } else href
    return addPathSegments(segments)
}