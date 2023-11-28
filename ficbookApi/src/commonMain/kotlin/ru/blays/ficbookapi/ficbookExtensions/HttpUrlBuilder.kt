package ru.blays.ficbookapi.ficbookExtensions

import okhttp3.HttpUrl
import okhttp3.Request
import ru.blays.ficbookapi.FICBOOK_HOST
import ru.blays.ficbookapi.HTTPS_SCHEME
import ru.blays.ficbookapi.okHttpDsl.httpUrl

fun ficbookUrl(block: HttpUrl.Builder.() -> Unit): HttpUrl {
    return httpUrl {
        scheme(HTTPS_SCHEME)
        host(FICBOOK_HOST)
        block()
    }
}


fun Request.Builder.ficbookUrl(
    block: HttpUrl.Builder.() -> Unit
) {
     url(
         ru.blays.ficbookapi.ficbookExtensions.ficbookUrl(block)
     )
}