package ru.blays.ficbook.reader.shared.data.repo.declaration

import ru.blays.ficbook.api.dataModels.ListResult
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.dto.NotificationCategoryStable
import ru.blays.ficbook.reader.shared.data.dto.NotificationModelStable
import ru.blays.ficbook.reader.shared.data.dto.NotificationType

interface INotificationsRepo {
    suspend fun get(category: NotificationType, page: Int): ApiResult<ListResult<NotificationModelStable>>

    suspend fun getAvailableCategories(): ApiResult<List<NotificationCategoryStable>>

    suspend fun deleteAll(): ApiResult<Unit>

    suspend fun readAll(): ApiResult<Unit>
}