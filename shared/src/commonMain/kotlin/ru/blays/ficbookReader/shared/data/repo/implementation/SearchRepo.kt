package ru.blays.ficbookReader.shared.data.repo.implementation

import ru.blays.ficbookReader.shared.data.dto.SearchedCharactersGroup
import ru.blays.ficbookReader.shared.data.dto.SearchedFandomModel
import ru.blays.ficbookReader.shared.data.dto.SearchedTagModel
import ru.blays.ficbookReader.shared.data.mappers.toStableModel
import ru.blays.ficbookReader.shared.data.repo.declaration.ISearchRepo
import ru.blays.ficbookapi.api.SearchApi
import ru.blays.ficbookapi.dataModels.SearchedCharactersModel
import ru.blays.ficbookapi.dataModels.SearchedFandomsModel
import ru.blays.ficbookapi.dataModels.SearchedTagsModel
import ru.blays.ficbookapi.result.ApiResult

class SearchRepo(
    private val api: SearchApi
): ISearchRepo {

    override suspend fun findFandoms(query: String): ApiResult<List<SearchedFandomModel>> {
        return when(
            val result = api.findFandoms(query)
        ) {
            is ApiResult.Success -> {
                val model = result.value.data.result.map(SearchedFandomsModel.Data.Result::toStableModel)
                ApiResult.Success(model)
            }
            is ApiResult.Error -> ApiResult.Error(result.exception)
        }
    }

    override suspend fun getCharacters(fandomIds: List<String>): ApiResult<List<SearchedCharactersGroup>> {
        return when(
            val result = api.getCharacters(fandomIds)
        ) {
            is ApiResult.Success -> {
                val groups = result.value.data.map(SearchedCharactersModel.Data::toStableModel)
                ApiResult.Success(groups)
            }
            is ApiResult.Error -> ApiResult.Error(result.exception)
        }
    }

    override suspend fun findTags(query: String): ApiResult<List<SearchedTagModel>> {
        return when(
            val result = api.findTags(query)
        ) {
            is ApiResult.Success -> {
                val model = result.value.data.tags.map(SearchedTagsModel.Data.Tag::toStableModel)
                ApiResult.Success(model)
            }
            is ApiResult.Error -> ApiResult.Error(result.exception)
        }
    }

}