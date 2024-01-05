package ru.blays.ficbookReader.shared.data.repo.declaration

import ru.blays.ficbookReader.shared.data.dto.FanficCardModelStable
import ru.blays.ficbookReader.shared.data.dto.SectionWithQuery
import ru.blays.ficbookapi.dataModels.ListResult
import ru.blays.ficbookapi.result.ApiResult

interface IFanficsListRepo {
    suspend fun get(href: String, page: Int): ApiResult<ListResult<FanficCardModelStable>>
    suspend fun get(section: SectionWithQuery, page: Int): ApiResult<ListResult<FanficCardModelStable>>
}