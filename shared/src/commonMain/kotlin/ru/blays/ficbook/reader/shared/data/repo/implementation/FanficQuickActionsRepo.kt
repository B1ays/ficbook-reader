package ru.blays.ficbook.reader.shared.data.repo.implementation

import ru.blays.ficbook.reader.shared.data.dto.FanficQuickActionsInfoModel
import ru.blays.ficbook.reader.shared.data.mappers.toStableModel
import ru.blays.ficbook.reader.shared.data.repo.declaration.IFanficQuickActionsRepo
import ru.blays.ficbook.api.api.FanficQuickActionsApi
import ru.blays.ficbook.api.result.ApiResult

class FanficQuickActionsRepo(
    private val api: FanficQuickActionsApi
): IFanficQuickActionsRepo {
    override suspend fun getInfo(fanficID: String): ApiResult<FanficQuickActionsInfoModel> {
        return when(
            val result = api.get(fanficID)
        ) {
            is ApiResult.Error -> ApiResult.failure(result.exception)
            is ApiResult.Success -> ApiResult.success(result.value.data.toStableModel())
        }
    }
}