package ru.blays.ficbookapi.api

import kotlinx.coroutines.coroutineScope
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import ru.blays.ficbookapi.COMMENT_ADD_HREF
import ru.blays.ficbookapi.PART_COMMENTS_HREF
import ru.blays.ficbookapi.dataModels.AjaxSimpleResult
import ru.blays.ficbookapi.dataModels.CommentModel
import ru.blays.ficbookapi.dataModels.ListResult
import ru.blays.ficbookapi.ficbookExtensions.ficbookUrl
import ru.blays.ficbookapi.json
import ru.blays.ficbookapi.okHttpDsl.*
import ru.blays.ficbookapi.parsers.CommentListParser
import ru.blays.ficbookapi.parsers.CommentParser
import ru.blays.ficbookapi.result.ApiResult


interface CommentsApi {
    suspend fun getForPart(partID: String, page: Int): ApiResult<ListResult<CommentModel>>
    suspend fun getAll(href: String, page: Int): ApiResult<ListResult<CommentModel>>
    suspend fun post(partID: String, text: String, followType: Int): ApiResult<Boolean>
    suspend fun delete(commentID: String): ApiResult<Boolean>
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

    override suspend fun post(
        partID: String,
        text: String,
        followType: Int
    ): ApiResult<Boolean> {
        require(followType in 0..2) {
            "Неизвестный тип подписки на комментарии"
        }

        val part1 = bodyPart(
            name = "part_id",
            value = partID
        )
        val part2 = bodyPart(
            name = "comment",
            value = text
        )
        val part3 = bodyPart(
            name = "follow_type",
            value = "$followType"
        )
        val postBody = multipartBody {
            addPart(part1)
            addPart(part2)
            addPart(part3)
            setType(MultipartBody.FORM)
        }

        return try {
            val response = client.post(
                url = ficbookUrl {
                    href(COMMENT_ADD_HREF)
                },
                body = postBody
            )
            val responseBody = response.body.stringOrThrow()
            val resultModel: AjaxSimpleResult = json.decodeFromString(responseBody)
            ApiResult.success(resultModel.result)
        } catch (e: Exception) {
            ApiResult.Error(e)
        }
    }

    override suspend fun delete(commentID: String): ApiResult<Boolean> = coroutineScope {
        return@coroutineScope try {
            val response = client.post(
                body = formBody {
                    add("comment_id", commentID)
                },
                url = ficbookUrl {
                    href("ajax/delete_comment")
                }
            )
            val body: String = response.body.stringOrThrow()
            val result: AjaxSimpleResult = json.decodeFromString(body)
            ApiResult.Success(result.result)
        } catch (e: Exception) {
            ApiResult.Error(e)
        }
    }
}