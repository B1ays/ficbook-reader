package ru.blays.ficbookapi.api

import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import ru.blays.ficbookapi.NOTIFICATIONS_HREF
import ru.blays.ficbookapi.dataModels.ListResult
import ru.blays.ficbookapi.dataModels.NotificationCategory
import ru.blays.ficbookapi.dataModels.NotificationModel
import ru.blays.ficbookapi.dataModels.NotificationType
import ru.blays.ficbookapi.ficbookExtensions.ficbookUrl
import ru.blays.ficbookapi.okHttpDsl.get
import ru.blays.ficbookapi.okHttpDsl.href
import ru.blays.ficbookapi.okHttpDsl.page
import ru.blays.ficbookapi.okHttpDsl.stringOrThrow
import ru.blays.ficbookapi.parsers.NotificationsParser
import ru.blays.ficbookapi.parsers.checkPageButtonsExists
import ru.blays.ficbookapi.result.ApiResult

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