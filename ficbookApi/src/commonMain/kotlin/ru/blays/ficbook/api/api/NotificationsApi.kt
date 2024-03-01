package ru.blays.ficbook.api.api

import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import ru.blays.ficbook.api.NOTIFICATIONS_HREF
import ru.blays.ficbook.api.dataModels.ListResult
import ru.blays.ficbook.api.dataModels.NotificationCategory
import ru.blays.ficbook.api.dataModels.NotificationModel
import ru.blays.ficbook.api.dataModels.NotificationType
import ru.blays.ficbook.api.ficbookExtensions.ficbookUrl
import ru.blays.ficbook.api.okHttpDsl.get
import ru.blays.ficbook.api.okHttpDsl.href
import ru.blays.ficbook.api.okHttpDsl.page
import ru.blays.ficbook.api.okHttpDsl.stringOrThrow
import ru.blays.ficbook.api.parsers.NotificationsParser
import ru.blays.ficbook.api.parsers.checkPageButtonsExists
import ru.blays.ficbook.api.result.ApiResult

interface NotificationsApi {

    suspend fun get(category: NotificationType, page: Int): ApiResult<ListResult<NotificationModel>>

    suspend fun getAvailableCategories(): ApiResult<List<NotificationCategory>>

    suspend fun deleteAll(): ApiResult<Unit>

    suspend fun readAll(): ApiResult<Unit>
}

class NotificationsApiImpl(
    private val client: OkHttpClient
): NotificationsApi {
    private val notificationsParser = NotificationsParser()

    override suspend fun get(
        category: NotificationType,
        page: Int
    ): ApiResult<ListResult<NotificationModel>> = coroutineScope {
        return@coroutineScope try {
            val response = client.get(
                url = ficbookUrl {
                    href(NOTIFICATIONS_HREF)
                    addQueryParameter("type", category.id.toString())
                    page(page)
                }
            )

            val body = response.body.stringOrThrow()
            val document = Jsoup.parse(body)
            val list = notificationsParser.parse(document)
            val pageButtons = checkPageButtonsExists(document)

            ApiResult.success(
                ListResult(
                    list = list,
                    hasNextPage = pageButtons.hasNext
                )
            )
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }

    override suspend fun getAvailableCategories(): ApiResult<List<NotificationCategory>> = coroutineScope {
        return@coroutineScope try {
            val response = client.get(
                url = ficbookUrl {
                    href(NOTIFICATIONS_HREF)
                }
            )
            val body = response.body.stringOrThrow()
            val document = Jsoup.parse(body)
            val categories = notificationsParser.getAvailableCategories(document)

            ApiResult.success(categories)
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }

    override suspend fun deleteAll(): ApiResult<Unit> = coroutineScope {
        return@coroutineScope try {
            client.get(
                url = ficbookUrl {
                    href("ajax/user_notifications/delete_all")
                }
            )
            ApiResult.success(Unit)
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }

    override suspend fun readAll(): ApiResult<Unit> = coroutineScope {
        return@coroutineScope try {
            client.get(
                url = ficbookUrl {
                    href("ajax/user_notifications/mark_old_all")
                }
            )
            ApiResult.success(Unit)
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }
}