package ru.blays.ficbook.reader.shared.data.repo.declaration

import ru.blays.ficbook.api.dataModels.ListResult
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.dto.FanficCardModelStable
import ru.blays.ficbook.reader.shared.data.dto.SectionWithQuery

interface IFanficsListRepo {
    suspend fun get(href: String, page: Int): ApiResult<ListResult<FanficCardModelStable>>
    suspend fun get(section: SectionWithQuery, page: Int): ApiResult<ListResult<FanficCardModelStable>>
}