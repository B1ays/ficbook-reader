package ru.blays.ficbook.reader.shared.data.repo.implementation

import ru.blays.ficbook.reader.shared.data.dto.NotificationCategoryStable
import ru.blays.ficbook.reader.shared.data.dto.NotificationModelStable
import ru.blays.ficbook.reader.shared.data.dto.NotificationType
import ru.blays.ficbook.reader.shared.data.mappers.toApiModel
import ru.blays.ficbook.reader.shared.data.mappers.toStableModel
import ru.blays.ficbook.reader.shared.data.repo.declaration.INotificationsRepo
import ru.blays.ficbook.api.api.NotificationsApi
import ru.blays.ficbook.api.dataModels.ListResult
import ru.blays.ficbook.api.dataModels.NotificationCategory
import ru.blays.ficbook.api.dataModels.NotificationModel
import ru.blays.ficbook.api.result.ApiResult

class NotificationsRepo(
    private val api: NotificationsApi
): INotificationsRepo {
    override suspend fun get(category: NotificationType, page: Int): ApiResult<ListResult<NotificationModelStable>> {
        val result = api.get(
            category = category.toApiModel(),
            page = page
        )
        return when(result) {
            is ApiResult.Error -> ApiResult.failure(result.exception)
            is ApiResult.Success -> ApiResult.success(
                ListResult(
                    list = result.value.list.map(NotificationModel::toStableModel),
                    hasNextPage = result.value.hasNextPage
                )
            )
        }
    }

    override suspend fun getAvailableCategories(): ApiResult<List<NotificationCategoryStable>> {
        return when(
            val result = api.getAvailableCategories()
        ) {
            is ApiResult.Error -> ApiResult.failure(result.exception)
            is ApiResult.Success -> ApiResult.success(result.value.map(NotificationCategory::toStableModel))
        }
    }

    override suspend fun deleteAll(): ApiResult<Unit> {
        return api.deleteAll()
    }

    override suspend fun readAll(): ApiResult<Unit> {
        return api.readAll()
    }
}