package ru.blays.ficbook.reader.shared.data.repo.declaration

import ru.blays.ficbook.api.dataModels.ListResult
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.dto.*

interface IAuthorProfileRepo {
    suspend fun getByHref(href: String): ApiResult<AuthorProfileModelStable>
    suspend fun getByID(id: String): ApiResult<AuthorProfileModelStable>

    suspend fun getBlogPosts(id: String, page: Int): ApiResult<ListResult<BlogPostCardModelStable>>
    suspend fun getBlogPage(userID: String, postID: String): ApiResult<BlogPostModelStable>
    suspend fun getPresents(id: String, page: Int): ApiResult<ListResult<AuthorPresentModelStable>>
    suspend fun getFanficsPresents(id: String, page: Int): ApiResult<ListResult<AuthorFanficPresentModelStable>>
    suspend fun getCommentsPresents(id: String, page: Int): ApiResult<ListResult<AuthorCommentPresentModelStable>>
}