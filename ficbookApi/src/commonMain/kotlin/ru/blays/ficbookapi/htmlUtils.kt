package ru.blays.ficbookapi

import kotlinx.coroutines.coroutineScope
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.use
import ru.blays.ficbookapi.dataModels.CookieModel
import ru.blays.ficbookapi.result.StringBody
import java.time.Duration

private val client = OkHttpClient
    .Builder()
    //.cookieJar()
    .callTimeout(Duration.ofMinutes(1))
    .connectTimeout(Duration.ofMinutes(1))
    .build()

internal suspend fun getHtmlBody(url: String): StringBody = coroutineScope {
    getHtmlBody(url.toHttpUrl())
}

internal suspend fun getHtmlBody(url: HttpUrl): StringBody = coroutineScope {
    val request = Request.Builder()
        .url(url)
        .build()

    getHtmlBody(
        request = request,
        cookieJar = null
    )

}

internal suspend fun getHtmlBody(
    request: Request,
    cookieJar: CookieJar?,
    block: (OkHttpClient.Builder.() -> Unit)? = null
    ): StringBody = coroutineScope {
    val builder = client.newBuilder()
    if (cookieJar != null) {
        builder.cookieJar(cookieJar)
    }
    block?.invoke(builder)
    val client = builder.build()

    try {
        client.newCall(request).execute().use {
            return@coroutineScope StringBody(it.body?.string())
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return@coroutineScope StringBody.empty
    }
}

internal suspend fun makeRequest(
    request: Request,
    cookieJar: CookieJar?,
    block: (OkHttpClient.Builder.() -> Unit)? = null
): Response? = coroutineScope {
    val builder = client.newBuilder()
    if (cookieJar != null) {
        builder.cookieJar(cookieJar)
    }
    block?.invoke(builder)
    val client = builder.build()

    try {
        return@coroutineScope client.newCall(request).execute()
    } catch (e: Exception) {
        e.printStackTrace()
        return@coroutineScope null
    }
}

internal class CustomCookieJar(): CookieJar {
    constructor(
        customCookies: List<CookieModel>
    ): this() {
        cookies.addAll(customCookies.map(::toOkHttpCookie))
    }

    private val cookies: MutableList<Cookie> = mutableListOf()
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookies
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        this.cookies.clear()
        this.cookies.addAll(cookies)
    }

    private fun toOkHttpCookie(cookieModel: CookieModel) = Cookie
        .Builder()
        .domain(FICBOOK_HOST)
        .name(cookieModel.name)
        .value(cookieModel.value)
        .build()

    private fun toCookieModel(cookie: Cookie) = CookieModel(
        name = cookie.name,
        value = cookie.value
    )
}