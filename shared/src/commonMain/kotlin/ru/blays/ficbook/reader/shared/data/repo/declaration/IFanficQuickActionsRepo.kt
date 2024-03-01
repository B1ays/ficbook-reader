package ru.blays.ficbook.reader.shared.data.repo.declaration

import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.dto.FanficQuickActionsInfoModel

interface IFanficQuickActionsRepo {
    suspend fun getInfo(fanficID: String): ApiResult<FanficQuickActionsInfoModel>
}