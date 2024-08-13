package ru.blays.ficbook.reader.shared.data.repo.implementation

import ru.blays.ficbook.api.api.CommentsApi
import ru.blays.ficbook.api.dataModels.CommentModel
import ru.blays.ficbook.api.dataModels.ListResult
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.dto.CommentModelStable
import ru.blays.ficbook.reader.shared.data.mappers.toStableModel
import ru.blays.ficbook.reader.shared.data.repo.declaration.ICommentsRepo

class CommentsRepo(
    val api: CommentsApi
): ICommentsRepo {
    override suspend fun getForPart(
        partID: String,
        page: Int
    ): ApiResult<ListResult<CommentModelStable>> {
        return when(
            val result = api.getForPart(partID, page)
        ) {
            is ApiResult.Error -> ApiResult.failure(result.exception)
            is ApiResult.Success -> {
                ApiResult.success(
                    ListResult(
                        list = result.value.list.map(CommentModel::toStableModel),
                        hasNextPage = result.value.hasNextPage
                    )
                )
            }
        }
    }

    override suspend fun getAll(
        href: String,
        page: Int
    ): ApiResult<ListResult<CommentModelStable>> {
        return when(
            val result = api.getAll(href, page)
        ) {
            is ApiResult.Error -> ApiResult.failure(result.exception)
            is ApiResult.Success -> {
                ApiResult.success(
                    ListResult(
                        list = result.value.list.map(CommentModel::toStableModel),
                        hasNextPage = result.value.hasNextPage
                    )
                )
            }
        }
    }

    override suspend fun postComment(
        partID: String,
        text: String,
        followType: Int
    ): ApiResult<Boolean> {
        return api.post(partID, text, followType)
    }

    override suspend fun delete(commentID: String): ApiResult<Boolean> {
        return api.delete(commentID)
    }

    override suspend fun like(commentID: String, like: Boolean): ApiResult<Boolean> {
        return api.like(commentID, like)
    }
}