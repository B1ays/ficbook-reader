package ru.blays.ficbook.reader.shared.data.repo.declaration

import ru.blays.ficbook.api.dataModels.ListResult
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.dto.PopularAuthorModelStable
import ru.blays.ficbook.reader.shared.data.dto.UserModelStable

interface IUsersRepo {
    suspend fun getFavouritesAuthors(page: Int): ApiResult<ListResult<UserModelStable>>

    suspend fun getPopularAuthors(): ApiResult<List<PopularAuthorModelStable>>

    suspend fun searchAuthor(name: String, page: Int): ApiResult<List<UserModelStable>>
}