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
        val result = api.getByHref(href)
        return result.map { it.toStableModel() }
    }

    override suspend fun getByID(
        id: String
    ): ApiResult<AuthorProfileModelStable> {
        val result = api.getByID(id)
        return result.map { it.toStableModel() }
    }

    override suspend fun changeFollow(follow: Boolean, id: String): ApiResult<Boolean> {
        return api.changeFollow(follow, id)
    }

    override suspend fun getBlogPosts(
        id: String,
        page: Int
    ): ApiResult<ListResult<BlogPostCardModelStable>> {
        val result = api.getBlogPosts(id, page)
        return result.map {
            ListResult(
                list = it.list.map(BlogPostCardModel::toStableModel),
                hasNextPage = it.hasNextPage
            )
        }
    }

    override suspend fun getBlogPage(userID: String, postID: String): ApiResult<BlogPostModelStable> {
        val result = api.getBlogPage(userID, postID)
        return result.map { it.toStableModel() }
    }

    override suspend fun getPresents(
        id: String,
        page: Int
    ): ApiResult<ListResult<AuthorPresentModelStable>> {
        val result = api.getAuthorPresents(id, page)
        return result.map {
            ListResult(
                list = it.list.map(AuthorPresentModel::toStableModel),
                hasNextPage = it.hasNextPage
            )
        }
    }

    override suspend fun getFanficsPresents(
        id: String,
        page: Int
    ): ApiResult<ListResult<AuthorFanficPresentModelStable>> {
        val result = api.getFanficsPresents(id, page)
        return result.map {
            ListResult(
                list = it.list.map(AuthorFanficPresentModel::toStableModel),
                hasNextPage = it.hasNextPage
            )
        }
    }

    override suspend fun getCommentsPresents(
        id: String,
        page: Int
    ): ApiResult<ListResult<AuthorCommentPresentModelStable>> {
        val result = api.getCommentsPresents(id, page)
        return result.map {
            ListResult(
                list = it.list.map(AuthorCommentPresentModel::toStableModel),
                hasNextPage = it.hasNextPage
            )
        }
    }
}