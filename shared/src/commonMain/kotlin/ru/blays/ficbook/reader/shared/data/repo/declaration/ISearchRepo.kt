package ru.blays.ficbook.reader.shared.data.repo.declaration

import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.dto.SearchedCharactersGroup
import ru.blays.ficbook.reader.shared.data.dto.SearchedFandomModel
import ru.blays.ficbook.reader.shared.data.dto.SearchedTagModel

interface ISearchRepo {
    suspend fun findFandoms(query: String): ApiResult<List<SearchedFandomModel>>

    suspend fun getCharacters(fandomIds: List<String>): ApiResult<List<SearchedCharactersGroup>>

    suspend fun findTags(query: String): ApiResult<List<SearchedTagModel>>
}