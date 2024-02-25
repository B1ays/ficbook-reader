package ru.blays.ficbookReader.shared.data.repo.declaration

import ru.blays.ficbookReader.shared.data.dto.SearchedCharacterModel
import ru.blays.ficbookReader.shared.data.dto.SearchedCharactersGroup
import ru.blays.ficbookReader.shared.data.dto.SearchedFandomModel
import ru.blays.ficbookReader.shared.data.dto.SearchedTagModel
import ru.blays.ficbookapi.result.ApiResult

interface ISearchRepo {
    suspend fun findFandoms(query: String): ApiResult<List<SearchedFandomModel>>

    suspend fun getCharacters(fandomIds: List<String>): ApiResult<List<SearchedCharactersGroup>>

    suspend fun findTags(query: String): ApiResult<List<SearchedTagModel>>
}