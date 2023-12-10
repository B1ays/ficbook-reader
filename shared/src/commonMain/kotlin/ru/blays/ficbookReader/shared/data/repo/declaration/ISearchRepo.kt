package ru.blays.ficbookReader.shared.data.repo.declaration

import ru.blays.ficbookReader.shared.data.dto.SearchedCharactersModel
import ru.blays.ficbookReader.shared.data.dto.SearchedFandomsModel
import ru.blays.ficbookReader.shared.data.dto.SearchedTagsModel
import ru.blays.ficbookapi.result.ApiResult

interface ISearchRepo {
    suspend fun findFandoms(query: String): ApiResult<SearchedFandomsModel>

    suspend fun getCharacters(fandomIds: List<String>): ApiResult<SearchedCharactersModel>

    suspend fun findTags(query: String): ApiResult<SearchedTagsModel>
}