package ru.blays.ficbookReader.shared.data.repo.declaration

import ru.blays.ficbookReader.shared.data.dto.*
import ru.blays.ficbookapi.dataModels.ListResult
import ru.blays.ficbookapi.result.ApiResult

interface IAuthorProfileRepo {
    suspend fun getByHref(href: String): ApiResult<AuthorProfileModelStable>
    suspend fun getByID(id: String): ApiResult<AuthorProfileModelStable>

    suspend fun getBlogPosts(id: String, page: Int): ApiResult<ListResult<BlogPostCardModelStable>>
    suspend fun getBlogPage(userID: String, postID: String): ApiResult<BlogPostModelStable>
    suspend fun getPresents(id: String, page: Int): ApiResult<ListResult<AuthorPresentModelStable>>
    suspend fun getFanficsPresents(id: String, page: Int): ApiResult<ListResult<AuthorFanficPresentModelStable>>
    suspend fun getCommentsPresents(id: String, page: Int): ApiResult<ListResult<AuthorCommentPresentModelStable>>
}