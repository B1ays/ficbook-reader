package ru.blays.ficbook.api.api

import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import ru.blays.ficbook.api.SEARCH_CHARACTERS_HREF
import ru.blays.ficbook.api.SEARCH_FANDOMS_HREF
import ru.blays.ficbook.api.SEARCH_TAGS_HREF
import ru.blays.ficbook.api.dataModels.SearchedCharactersModel
import ru.blays.ficbook.api.dataModels.SearchedFandomsModel
import ru.blays.ficbook.api.dataModels.SearchedTagsModel
import ru.blays.ficbook.api.ficbookExtensions.ficbookUrl
import ru.blays.ficbook.api.json
import ru.blays.ficbook.api.okHttpDsl.formBody
import ru.blays.ficbook.api.okHttpDsl.href
import ru.blays.ficbook.api.okHttpDsl.post
import ru.blays.ficbook.api.okHttpDsl.stringOrThrow
import ru.blays.ficbook.api.result.ApiResult

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