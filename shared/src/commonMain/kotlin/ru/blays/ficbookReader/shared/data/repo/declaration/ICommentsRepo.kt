package ru.blays.ficbookReader.shared.data.repo.declaration

import ru.blays.ficbookReader.shared.data.dto.CommentModelStable
import ru.blays.ficbookapi.dataModels.ListResult
import ru.blays.ficbookapi.result.ApiResult

interface ICommentsRepo {
    suspend fun getForPart(partID: String, page: Int): ApiResult<ListResult<CommentModelStable>>
    suspend fun getAll(href: String, page: Int): ApiResult<ListResult<CommentModelStable>>
}