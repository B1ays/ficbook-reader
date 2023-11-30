package ru.blays.ficbookReader.shared.data.repo.declaration

import ru.blays.ficbookReader.shared.data.dto.PopularAuthorModelStable
import ru.blays.ficbookReader.shared.data.dto.UserModelStable
import ru.blays.ficbookapi.dataModels.ListResult
import ru.blays.ficbookapi.result.ApiResult

interface IUsersRepo {
    suspend fun getFavouritesAuthors(page: Int): ApiResult<ListResult<UserModelStable>>

    suspend fun getPopularAuthors(): ApiResult<List<PopularAuthorModelStable>>

    suspend fun searchAuthor(name: String, page: Int): ApiResult<List<UserModelStable>>
}