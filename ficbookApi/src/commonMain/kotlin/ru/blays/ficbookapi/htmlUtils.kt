package ru.blays.ficbookapi

import kotlinx.coroutines.coroutineScope
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.use
import ru.blays.ficbookapi.dataModels.CookieModel
import java.time.Duration

private val client = OkHttpClient
    .Builder()
    //.cookieJar()
    .callTimeout(Duration.ofMinutes(1))
    .connectTimeout(Duration.ofMinutes(1))
    .build()

internal suspend fun getHtmlBody(url: String): String? = coroutineScope {
    getHtmlBody(url.toHttpUrl())
}

internal suspend fun getHtmlBody(url: HttpUrl): String? = coroutineScope {
    val request = Request.Builder()
        .url(url)
        .build()

    getHtmlBody(request)

}

internal suspend fun getHtmlBody(
    request: Request,
    block: (OkHttpClient.Builder.() -> Unit)? = null
    ): String? = coroutineScope {
    val builder = client.newBuilder()
    block?.invoke(builder)
    val client = builder.build()

    try {
        client.newCall(request).execute().use {
            return@coroutineScope it.body?.string()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return@coroutineScope null
    }
}

internal suspend fun makeRequest(
    request: Request,
    block: (OkHttpClient.Builder.() -> Unit)? = null
): Response? = coroutineScope {
    val builder = client.newBuilder()
    block?.invoke(builder)
    val client = builder.build()

    try {
        return@coroutineScope client.newCall(request).execute()
    } catch (e: Exception) {
        e.printStackTrace()
        return@coroutineScope null
    }
}

class CustomCookieJar(
    private val cookies: List<CookieModel>,
    private val onCookieSave: (cookies: List<CookieModel>) -> Unit
): CookieJar {
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookies.map(::toOkHttpCookie)
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        onCookieSave(cookies.map(::toCookieModel))
    }

    private fun toOkHttpCookie(cookieModel: CookieModel) = Cookie
        .Builder()
        .name(cookieModel.name)
        .value(cookieModel.value)
        .build()

    private fun toCookieModel(cookie: Cookie) = CookieModel(
        name = cookie.name,
        value = cookie.value
    )
}