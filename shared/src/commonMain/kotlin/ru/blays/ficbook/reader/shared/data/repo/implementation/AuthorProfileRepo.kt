package ru.blays.ficbook.reader.shared.data.repo.implementation

import ru.blays.ficbook.api.api.AuthorProfileApi
import ru.blays.ficbook.api.dataModels.*
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.dto.*
import ru.blays.ficbook.reader.shared.data.mappers.toStableModel
import ru.blays.ficbook.reader.shared.data.repo.declaration.IAuthorProfileRepo

class AuthorProfileRepo(
    private val api: AuthorProfileApi
): IAuthorProfileRepo {
    override suspend fun getByHref(
        href: String
    ): ApiResult<AuthorProfileModelStable> {
        return when(
            val result = api.getByHref(href)
        ) {
            is ApiResult.Error -> ApiResult.failure(result.exception)
            is ApiResult.Success -> ApiResult.success(result.value.toStableModel())
        }
    }

    override suspend fun getByID(
        id: String
    ): ApiResult<AuthorProfileModelStable> {
        return when(
            val result = api.getByID(id)
        ) {
            is ApiResult.Error -> ApiResult.failure(result.exception)
            is ApiResult.Success -> ApiResult.success(
                value = result.value.toStableModel()
            )
        }
    }

    override suspend fun changeFollow(follow: Boolean, id: String): ApiResult<Boolean> {
        return api.changeFollow(follow, id)
    }

    override suspend fun getBlogPosts(
        id: String,
        page: Int
    ): ApiResult<ListResult<BlogPostCardModelStable>> {
        return when(
            val result = api.getBlogPosts(id, page)
        ) {
            is ApiResult.Error -> ApiResult.failure(result.exception)
            is ApiResult.Success -> ApiResult.success(
                ListResult(
                    list = result.value.list.map(BlogPostCardModel::toStableModel),
                    hasNextPage = result.value.hasNextPage
                )
            )
        }
    }

    override suspend fun getBlogPage(userID: String, postID: String): ApiResult<BlogPostModelStable> {
        return when(
            val result = api.getBlogPage(userID, postID)
        ) {
            is ApiResult.Error -> ApiResult.failure(result.exception)
            is ApiResult.Success -> ApiResult.success(result.value.toStableModel())
        }
    }

    override suspend fun getPresents(
        id: String,
        page: Int
    ): ApiResult<ListResult<AuthorPresentModelStable>> {
        return when(
            val result = api.getAuthorPresents(id, page)
        ) {
            is ApiResult.Error -> ApiResult.failure(result.exception)
            is ApiResult.Success -> ApiResult.success(
                ListResult(
                    list = result.value.list.map(AuthorPresentModel::toStableModel),
                    hasNextPage = result.value.hasNextPage
                )
            )
        }
    }

    override suspend fun getFanficsPresents(
        id: String,
        page: Int
    ): ApiResult<ListResult<AuthorFanficPresentModelStable>> {
        return when(
            val result = api.getFanficsPresents(id, page)
        ) {
            is ApiResult.Error -> ApiResult.failure(result.exception)
            is ApiResult.Success -> ApiResult.success(
                ListResult(
                    list = result.value.list.map(AuthorFanficPresentModel::toStableModel),
                    hasNextPage = result.value.hasNextPage
                )
            )
        }
    }

    override suspend fun getCommentsPresents(
        id: String,
        page: Int
    ): ApiResult<ListResult<AuthorCommentPresentModelStable>> {
        return when(
            val result = api.getCommentsPresents(id, page)
        ) {
            is ApiResult.Error -> ApiResult.failure(result.exception)
            is ApiResult.Success -> ApiResult.success(
                ListResult(
                    list = result.value.list.map(AuthorCommentPresentModel::toStableModel),
                    hasNextPage = result.value.hasNextPage
                )
            )
        }
    }
}