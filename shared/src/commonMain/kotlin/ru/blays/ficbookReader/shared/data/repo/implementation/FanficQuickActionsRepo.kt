package ru.blays.ficbookReader.shared.data.repo.implementation

import ru.blays.ficbookReader.shared.data.dto.FanficQuickActionsInfoModel
import ru.blays.ficbookReader.shared.data.mappers.toStableModel
import ru.blays.ficbookReader.shared.data.repo.declaration.IFanficQuickActionsRepo
import ru.blays.ficbookapi.api.FanficQuickActionsApi
import ru.blays.ficbookapi.result.ApiResult

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