package ru.blays.ficbookapi.api

import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import ru.blays.ficbookapi.SEARCH_CHARACTERS_HREF
import ru.blays.ficbookapi.SEARCH_FANDOMS_HREF
import ru.blays.ficbookapi.SEARCH_TAGS_HREF
import ru.blays.ficbookapi.dataModels.SearchedCharactersModel
import ru.blays.ficbookapi.dataModels.SearchedFandomsModel
import ru.blays.ficbookapi.dataModels.SearchedTagsModel
import ru.blays.ficbookapi.ficbookExtensions.ficbookUrl
import ru.blays.ficbookapi.json
import ru.blays.ficbookapi.okHttpDsl.formBody
import ru.blays.ficbookapi.okHttpDsl.href
import ru.blays.ficbookapi.okHttpDsl.post
import ru.blays.ficbookapi.okHttpDsl.stringOrThrow
import ru.blays.ficbookapi.result.ApiResult

interface SearchApi {
    suspend fun findFandoms(query: String): ApiResult<SearchedFandomsModel>
    suspend fun getCharacters(fandomIds: List<String>): ApiResult<SearchedCharactersModel>
    suspend fun findTags(query: String): ApiResult<SearchedTagsModel>
}

class SearchApiImpl(
    private val client: OkHttpClient
): SearchApi {
    override suspend fun findFandoms(
        query: String
    ): ApiResult<SearchedFandomsModel> = coroutineScope {
        return@coroutineScope try {
            val response = client.post(
                url = ficbookUrl {
                    href(SEARCH_FANDOMS_HREF)
                },
                body = formBody {
                    add("group_id", "")
                    add("show_empty", "false")
                    add("show_originals", "false")
                    add("q", query)
                    add("page", "1")
                }
            )
            val body = response.body.stringOrThrow()
            val result: SearchedFandomsModel = json.decodeFromString(body)
            ApiResult.success(result)
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }

    override suspend fun getCharacters(
        fandomIds: List<String>
    ): ApiResult<SearchedCharactersModel> = coroutineScope {
        return@coroutineScope try {
            val response = client.post(
                url = ficbookUrl {
                    href(SEARCH_CHARACTERS_HREF)
                },
                body = formBody {
                    fandomIds.forEach { id ->
                        add("fandomIds[]", id)
                    }
                }
            )
            val body = response.body.stringOrThrow()
            val result: SearchedCharactersModel = json.decodeFromString(body)
            ApiResult.success(result)
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }

    override suspend fun findTags(
        query: String
    ): ApiResult<SearchedTagsModel> = coroutineScope {
        return@coroutineScope try {
            val response = client.post(
                url = ficbookUrl {
                    href(SEARCH_TAGS_HREF)
                },
                body = formBody {
                    add("title", query)
                    add("page", "1")
                }
            )
            val body = response.body.stringOrThrow()
            val result: SearchedTagsModel = json.decodeFromString(body)
            ApiResult.success(result)
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }
}