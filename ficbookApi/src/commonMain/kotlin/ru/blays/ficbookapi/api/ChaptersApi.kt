package ru.blays.ficbookapi.api

import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import ru.blays.ficbookapi.ficbookExtensions.ficbookUrl
import ru.blays.ficbookapi.okHttpDsl.get
import ru.blays.ficbookapi.okHttpDsl.href
import ru.blays.ficbookapi.okHttpDsl.stringOrThrow
import ru.blays.ficbookapi.parsers.SeparateChapterParser
import ru.blays.ficbookapi.result.ApiResult

interface ChaptersApi {
    suspend fun getChapterText(href: String): ApiResult<String>
}

class ChaptersApiImpl(
    private val client: OkHttpClient
): ChaptersApi {
    private val chapterTextParser = SeparateChapterParser()

    override suspend fun getChapterText(href: String): ApiResult<String> = coroutineScope {
        return@coroutineScope try {
            val response = client.get(
                url = ficbookUrl {
                    href(href)
                }
            )
            val body: String = response.body.stringOrThrow()
            val document = Jsoup.parse(body)
            val text = chapterTextParser.parse(document)
            ApiResult.success(text)
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }
}