package ru.blays.ficbookapi

import kotlinx.coroutines.coroutineScope
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.use

private val client = OkHttpClient
    .Builder()
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

internal suspend fun getHtmlBody(request: Request): String? = coroutineScope {
    val client = client

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
    val builder = OkHttpClient
        .Builder()
    block?.invoke(builder)
    val client = builder.build()


    try {
        return@coroutineScope client.newCall(request).execute()
    } catch (e: Exception) {
        e.printStackTrace()
        return@coroutineScope null
    }
}