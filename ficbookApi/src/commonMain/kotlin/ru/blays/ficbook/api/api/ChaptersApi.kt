package ru.blays.ficbook.api.api

import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import ru.blays.ficbook.api.ficbookExtensions.ficbookUrl
import ru.blays.ficbook.api.okHttpDsl.get
import ru.blays.ficbook.api.okHttpDsl.href
import ru.blays.ficbook.api.okHttpDsl.stringOrThrow
import ru.blays.ficbook.api.parsers.ChapterParser
import ru.blays.ficbook.api.result.ApiResult


interface ChaptersApi {
    suspend fun getChapterText(href: String): ApiResult<String>
}

class ChaptersApiImpl(
    private val client: OkHttpClient
): ChaptersApi {
    private val chapterTextParser = ChapterParser()

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