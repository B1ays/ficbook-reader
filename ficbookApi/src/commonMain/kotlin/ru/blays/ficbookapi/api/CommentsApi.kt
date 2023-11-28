package ru.blays.ficbookapi.api

import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import ru.blays.ficbookapi.PART_COMMENTS_HREF
import ru.blays.ficbookapi.dataModels.CommentModel
import ru.blays.ficbookapi.dataModels.ListResult
import ru.blays.ficbookapi.ficbookExtensions.ficbookUrl
import ru.blays.ficbookapi.okHttpDsl.*
import ru.blays.ficbookapi.parsers.CommentListParser
import ru.blays.ficbookapi.parsers.CommentParser
import ru.blays.ficbookapi.result.ApiResult


interface CommentsApi {
    suspend fun getForPart(partID: String, page: Int): ApiResult<ListResult<CommentModel>>
    suspend fun getAll(href: String, page: Int): ApiResult<ListResult<CommentModel>>
}

class CommentsApiImpl(
    private val client: OkHttpClient
): CommentsApi {
    private val commentsParser = CommentParser()
    private val commentsListParser = CommentListParser()
    override suspend fun getForPart(
        partID: String,
        page: Int
    ): ApiResult<ListResult<CommentModel>> = coroutineScope {
        return@coroutineScope try {
            val response = client.post(
                body = formBody {
                    add("id", partID)
                    add("page", page.toString())
                },
                url = ficbookUrl {
                    href(PART_COMMENTS_HREF)
                    page(page)
                }
            )
            val body: String = response.body.stringOrThrow()
            val document = Jsoup.parse(body)
            val elements = commentsListParser.parse(document)
            val comments = elements.map { commentsParser.parse(it) }
            val hasNextPage = body.isNotEmpty()
            ApiResult.Success(
                ListResult(
                    list = comments,
                    hasNextPage = hasNextPage
                )
            )
        } catch (e: Exception) {
            ApiResult.Error(e)
        }
    }

    override suspend fun getAll(
        href: String,
        page: Int
    ): ApiResult<ListResult<CommentModel>> = coroutineScope {
        val trimmedHref = href.substringBefore('#')
        return@coroutineScope try {
            val response = client.get(
                url = ficbookUrl {
                    href(trimmedHref)
                    page(page)
                }
            )
            val body: String = response.body.stringOrThrow()
            val document = Jsoup.parse(body)
            val elements = commentsListParser.parse(document)
            val comments = elements.map { commentsParser.parse(it) }
            val hasNextPage = body.isNotEmpty()
            ApiResult.Success(
                ListResult(
                    list = comments,
                    hasNextPage = hasNextPage
                )
            )
        } catch (e: Exception) {
            ApiResult.Error(e)
        }
    }
}