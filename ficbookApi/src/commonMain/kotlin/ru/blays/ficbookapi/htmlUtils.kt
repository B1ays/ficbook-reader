package ru.blays.ficbookapi

import kotlinx.coroutines.coroutineScope
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.use

var chuckerInterceptor: Interceptor? = null

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
    val client = OkHttpClient.Builder().run {
        if(chuckerInterceptor != null) addInterceptor(chuckerInterceptor!!)
        else this
    }.build()


    try {
        client.newCall(request).execute().use {
            return@coroutineScope it.body?.string()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return@coroutineScope null
    }
}

internal suspend fun makeRequest(request: Request): Response? = coroutineScope {
    val client = OkHttpClient.Builder().run {
        if(chuckerInterceptor != null) addInterceptor(chuckerInterceptor!!)
        else this
    }.build()

    try {
        client.newCall(request).execute().use {
            return@coroutineScope it
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return@coroutineScope null
    }
}