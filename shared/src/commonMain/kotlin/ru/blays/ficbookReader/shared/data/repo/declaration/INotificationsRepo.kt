package ru.blays.ficbookReader.shared.data.repo.declaration

import ru.blays.ficbookReader.shared.data.dto.NotificationCategoryStable
import ru.blays.ficbookReader.shared.data.dto.NotificationModelStable
import ru.blays.ficbookReader.shared.data.dto.NotificationType
import ru.blays.ficbookapi.dataModels.ListResult
import ru.blays.ficbookapi.result.ApiResult

interface INotificationsRepo {
    suspend fun get(category: NotificationType, page: Int): ApiResult<ListResult<NotificationModelStable>>

    suspend fun getAvailableCategories(): ApiResult<List<NotificationCategoryStable>>

    suspend fun deleteAll(): ApiResult<Unit>

    suspend fun readAll(): ApiResult<Unit>
}