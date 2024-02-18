package ru.blays.ficbookReader.shared.data.repo.declaration

import ru.blays.ficbookReader.shared.data.dto.FanficQuickActionsInfoModel
import ru.blays.ficbookapi.result.ApiResult

interface IFanficQuickActionsRepo {
    suspend fun getInfo(fanficID: String): ApiResult<FanficQuickActionsInfoModel>
}