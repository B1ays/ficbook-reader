package ru.blays.ficbookReader.shared.data.repo.implementation

import kotlinx.serialization.json.Json
import ru.blays.ficbookReader.shared.data.dto.SearchedCharactersModel
import ru.blays.ficbookReader.shared.data.dto.SearchedFandomsModel
import ru.blays.ficbookReader.shared.data.dto.SearchedTagsModel
import ru.blays.ficbookReader.shared.data.repo.declaration.ISearchRepo
import ru.blays.ficbookapi.api.SearchApi
import ru.blays.ficbookapi.result.ApiResult

class SearchRepo(
    private val api: SearchApi
): ISearchRepo {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun findFandoms(query: String): ApiResult<SearchedFandomsModel> {
        return when(
            val result = api.findFandoms(query)
        ) {
            is ApiResult.Success -> {
                val model: SearchedFandomsModel = json.decodeFromString(result.value)
                ApiResult.Success(model)
            }
            is ApiResult.Error -> ApiResult.Error(result.exception)
        }
    }

    override suspend fun getCharacters(fandomIds: List<String>): ApiResult<SearchedCharactersModel> {
        return when(
            val result = api.getCharacters(fandomIds)
        ) {
            is ApiResult.Success -> {
                val model: SearchedCharactersModel = json.decodeFromString(result.value)
                ApiResult.Success(model)
            }
            is ApiResult.Error -> ApiResult.Error(result.exception)
        }
    }

    override suspend fun findTags(query: String): ApiResult<SearchedTagsModel> {
        return when(
            val result = api.findTags(query)
        ) {
            is ApiResult.Success -> {
                val model: SearchedTagsModel = json.decodeFromString(result.value)
                ApiResult.Success(model)
            }
            is ApiResult.Error -> ApiResult.Error(result.exception)
        }
    }
}