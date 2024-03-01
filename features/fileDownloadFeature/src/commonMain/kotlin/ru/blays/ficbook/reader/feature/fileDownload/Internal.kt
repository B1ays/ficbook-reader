package ru.blays.ficbook.reader.feature.fileDownload

import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.mp.KoinPlatform.getKoin
import java.io.InputStream

internal suspend fun makeDownloadResponse(url: String): DownloadResponseResult? = coroutineScope {
    val client: OkHttpClient by getKoin().inject()

    val request = Request.Builder()
        .addHeader("accept-language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
        .addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
        .url(url)
        .build()

    return@coroutineScope try {
        val response = client.newCall(request).execute()

        if(response.code != 200) {
            return@coroutineScope null
        }

        val body = response.body!!

        DownloadResponseResult(
            inputStream = body.byteStream(),
            contentLength = body.contentLength()
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

internal data class DownloadResponseResult(
    val inputStream: InputStream,
    val contentLength: Long
)