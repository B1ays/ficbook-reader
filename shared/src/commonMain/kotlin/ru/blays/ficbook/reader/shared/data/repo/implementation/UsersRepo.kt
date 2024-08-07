package ru.blays.ficbook.reader.shared.data.repo.implementation

import ru.blays.ficbook.reader.shared.data.dto.PopularAuthorModelStable
import ru.blays.ficbook.reader.shared.data.dto.UserModelStable
import ru.blays.ficbook.reader.shared.data.mappers.toStableModel
import ru.blays.ficbook.reader.shared.data.mappers.toUserModel
import ru.blays.ficbook.reader.shared.data.repo.declaration.IUsersRepo
import ru.blays.ficbook.api.api.UsersApi
import ru.blays.ficbook.api.dataModels.AuthorSearchResult
import ru.blays.ficbook.api.dataModels.ListResult
import ru.blays.ficbook.api.dataModels.PopularAuthorModel
import ru.blays.ficbook.api.dataModels.UserModel
import ru.blays.ficbook.api.result.ApiResult

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