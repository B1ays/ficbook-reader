package ru.blays.ficbook.api.api

import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import ru.blays.ficbook.api.dataModels.AjaxSimpleResult
import ru.blays.ficbook.api.dataModels.FanficPageModel
import ru.blays.ficbook.api.ficbookExtensions.ficbookUrl
import ru.blays.ficbook.api.json
import ru.blays.ficbook.api.okHttpDsl.*
import ru.blays.ficbook.api.parsers.FanficPageParser
import ru.blays.ficbook.api.result.ApiResult


interface FanficPageApi {
    suspend fun getByHref(href: String): ApiResult<FanficPageModel>
    suspend fun getById(id: String): ApiResult<FanficPageModel>

    suspend fun mark(mark: Boolean, fanficID: String): Boolean
    suspend fun follow(follow: Boolean, fanficID: String): Boolean
    suspend fun vote(vote: Boolean, partID: String): Boolean
    suspend fun read(read: Boolean, fanficID: String): Boolean
}

internal class FanficPageApiImpl(
    private val client: OkHttpClient
): FanficPageApi {
    private val fanficPageParser = FanficPageParser()

    override suspend fun getByHref(href: String): ApiResult<FanficPageModel> = coroutineScope {
        return@coroutineScope try {
            val response = client.get(
                url = ficbookUrl {
                    href(href)
                }
            )
            val body: String = response.body.stringOrThrow()
            val document = Jsoup.parse(body)
            val fanficPage = fanficPageParser.parse(document)
            ApiResult.success(fanficPage)
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }

    override suspend fun getById(id: String): ApiResult<FanficPageModel> {
        return getByHref("readfic/$id")
    }

    override suspend fun mark(
        mark: Boolean,
        fanficID: String
    ): Boolean = coroutineScope {
        return@coroutineScope try {
            val action = if (mark) "add" else "remove"
            val response = client.post(
                body = formBody {
                    add("fanfic_id", fanficID)
                    add("action", action)
                }
            ) {
                ficbookUrl {
                    href("ajax/mark")
                }
                header("Referer", "https://ficbook.net/readfic/$fanficID")
            }
            val body = response.body.stringOrThrow()
            val result: AjaxSimpleResult = json.decodeFromString(body)
            result.result
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun follow(
        follow: Boolean,
        fanficID: String
    ): Boolean = coroutineScope {
        return@coroutineScope try {
            val href = if (follow) "fanfic_follow/follow" else "fanfic_follow/unfollow"
            val response = client.post(
                body = formBody {
                    add("fanfic_id", fanficID)
                }
            ) {
                ficbookUrl {
                    href(href)
                }
                header("Referer", "https://ficbook.net/readfic/$fanficID")
            }
            val body = response.body.stringOrThrow()
            val result: AjaxSimpleResult = json.decodeFromString(body)
            result.result
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun vote(
        vote: Boolean,
        partID: String
    ): Boolean = coroutineScope {
        return@coroutineScope try {
            val href = if (vote) "fanfics/continue_votes/add" else "fanfics/continue_votes/remove"
            val response = client.post(
                body = formBody {
                    add("part_id", partID)
                }
            ) {
                ficbookUrl {
                    href(href)
                }
            }
            val body = response.body.stringOrThrow()
            val result: AjaxSimpleResult = json.decodeFromString(body)
            result.result
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun read(
        read: Boolean,
        fanficID: String
    ): Boolean = coroutineScope {
        return@coroutineScope try {
            val href = if (read) "/fanfic_read/read" else "/fanfic_read/unread"
            val response = client.post(
                body = formBody {
                    add("fanfic_id", fanficID)
                },
                url = ficbookUrl {
                    href(href)
                }
            )
            val body = response.body.stringOrThrow()
            val result: AjaxSimpleResult = Json.decodeFromString(body)
            result.result
        } catch (e: Exception) {
            false
        }
    }
}