package ru.blays.ficbookReader.shared.data.repo.implementation

import ru.blays.ficbookReader.shared.data.dto.CommentModelStable
import ru.blays.ficbookReader.shared.data.mappers.toStableModel
import ru.blays.ficbookReader.shared.data.repo.declaration.ICommentsRepo
import ru.blays.ficbookapi.api.CommentsApi
import ru.blays.ficbookapi.dataModels.CommentModel
import ru.blays.ficbookapi.dataModels.ListResult
import ru.blays.ficbookapi.result.ApiResult

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
}