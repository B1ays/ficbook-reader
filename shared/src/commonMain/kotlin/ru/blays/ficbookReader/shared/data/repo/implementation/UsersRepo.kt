package ru.blays.ficbookReader.shared.data.repo.implementation

import ru.blays.ficbookReader.shared.data.dto.PopularAuthorModelStable
import ru.blays.ficbookReader.shared.data.dto.UserModelStable
import ru.blays.ficbookReader.shared.data.mappers.toStableModel
import ru.blays.ficbookReader.shared.data.mappers.toUserModel
import ru.blays.ficbookReader.shared.data.repo.declaration.IUsersRepo
import ru.blays.ficbookapi.api.UsersApi
import ru.blays.ficbookapi.dataModels.AuthorSearchResult
import ru.blays.ficbookapi.dataModels.ListResult
import ru.blays.ficbookapi.dataModels.PopularAuthorModel
import ru.blays.ficbookapi.dataModels.UserModel
import ru.blays.ficbookapi.result.ApiResult

class UsersRepo(
    private val api: UsersApi
): IUsersRepo {
    override suspend fun getFavouritesAuthors(
        page: Int
    ): ApiResult<ListResult<UserModelStable>> {
        return when(
            val result = api.getFavouritesAuthors(page)
        ) {
            is ApiResult.Success -> {
                ApiResult.success(
                    ListResult(
                        list = result.value.list.map(UserModel::toStableModel),
                        hasNextPage = result.value.hasNextPage
                    )
                )
            }
            is ApiResult.Error -> ApiResult.Error(result.exception)
        }
    }

    override suspend fun getPopularAuthors(): ApiResult<List<PopularAuthorModelStable>> {
        return when(
            val result = api.getPopularAuthors()
        ) {
            is ApiResult.Error -> ApiResult.failure(result.exception)
            is ApiResult.Success -> {
                ApiResult.success(
                    value = result.value.map(PopularAuthorModel::toStableModel)
                )
            }
        }
    }

    override suspend fun searchAuthor(
        name: String,
        page: Int
    ): ApiResult<List<UserModelStable>> {
        return when(
            val result = api.searchAuthor(name, page)
        ) {
            is ApiResult.Error -> ApiResult.failure(result.exception)
            is ApiResult.Success -> {
                ApiResult.success(
                    value = result.value.data.result.map(AuthorSearchResult.Data.Result::toUserModel)
                )
            }
        }
    }
}