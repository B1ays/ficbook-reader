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
import ru.blays.ficbook.api.result.ResponseResult


interface ChaptersApi {
    suspend fun getChapterText(href: String): ApiResult<String>
    suspend fun getChapterText(fanficID: String, id: String): ResponseResult<String>

    suspend fun getChapterHtml(href: String): ResponseResult<String>
    suspend fun getChapterHtml(fanficID: String, id: String): ResponseResult<String>
}

internal class ChaptersApiImpl(
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
            val text = chapterTextParser.parseText(document)
            ApiResult.success(text)
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }

    override suspend fun getChapterText(
        fanficID: String,
        id: String
    ): ResponseResult<String> = coroutineScope {
        return@coroutineScope try {
            val response = client.get(
                url = ficbookUrl {
                    href("readfic/$fanficID/$id")
                }
            )
            if(response.code != 200) {
                return@coroutineScope ResponseResult.Error(response.code)
            }
            val body: String = response.body.stringOrThrow()
            val document = Jsoup.parse(body)
            val text = chapterTextParser.parseText(document)
            ResponseResult.Success(text)
        } catch (e: Exception) {
            ResponseResult.Error(-1)
        }
    }

    override suspend fun getChapterHtml(href: String): ResponseResult<String> = coroutineScope {
        return@coroutineScope try {
            val response = client.get(
                url = ficbookUrl {
                    href(href)
                }
            )
            if(response.code != 200) {
                return@coroutineScope ResponseResult.Error(response.code)
            }
            val body: String = response.body.stringOrThrow()
            val document = Jsoup.parse(body)
            val fullHtml = chapterTextParser.parseHtml(document)
            ResponseResult.Success(fullHtml)
        } catch (e: Exception) {
            ResponseResult.Error(-1)
        }
    }

    override suspend fun getChapterHtml(
        fanficID: String,
        id: String
    ): ResponseResult<String> = coroutineScope {
        return@coroutineScope try {
            val response = client.get(
                url = ficbookUrl {
                    href("readfic/$fanficID/$id")
                }
            )
            if(response.code != 200) {
                return@coroutineScope ResponseResult.Error(response.code)
            }
            val body: String = response.body.stringOrThrow()
            val document = Jsoup.parse(body)
            val fullHtml = chapterTextParser.parseHtml(document)
            ResponseResult.Success(fullHtml)
        } catch (e: Exception) {
            ResponseResult.Error(-1)
        }
    }
}