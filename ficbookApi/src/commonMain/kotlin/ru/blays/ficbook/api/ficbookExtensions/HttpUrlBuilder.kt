package ru.blays.ficbook.api.ficbookExtensions

import okhttp3.HttpUrl
import okhttp3.Request
import ru.blays.ficbook.api.FICBOOK_HOST
import ru.blays.ficbook.api.HTTPS_SCHEME
import ru.blays.ficbook.api.okHttpDsl.httpUrl

internal fun ficbookUrl(
    block: HttpUrl.Builder.() -> Unit
): HttpUrl {
    return httpUrl {
        scheme(HTTPS_SCHEME)
        host(FICBOOK_HOST)
        block()
    }
}

internal fun Request.Builder.ficbookUrl(
    block: HttpUrl.Builder.() -> Unit
) = url(ru.blays.ficbook.api.ficbookExtensions.ficbookUrl(block))
