package ru.blays.ficbook.reader.shared.data.repo.declaration

import ru.blays.ficbook.api.dataModels.ListResult
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.dto.CommentModelStable

interface ICommentsRepo {
    suspend fun getForPart(partID: String, page: Int): ApiResult<ListResult<CommentModelStable>>
    suspend fun getAll(href: String, page: Int): ApiResult<ListResult<CommentModelStable>>
    suspend fun postComment(partID: String, text: String, followType: Int): ApiResult<Boolean>
    suspend fun delete(commentID: String): ApiResult<Boolean>
}