package ru.blays.ficbookapi.api

import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import ru.blays.ficbookapi.data.SectionWithQuery
import ru.blays.ficbookapi.dataModels.*
import ru.blays.ficbookapi.ficbookExtensions.ficbookUrl
import ru.blays.ficbookapi.okHttpDsl.*
import ru.blays.ficbookapi.parsers.CollectionListParser
import ru.blays.ficbookapi.parsers.CollectionSortParamsParser
import ru.blays.ficbookapi.parsers.checkPageButtonsExists
import ru.blays.ficbookapi.result.ApiResult

interface CollectionsApi {
    suspend fun getCollections(section: SectionWithQuery, page: Int): ApiResult<ListResult<CollectionModel>>

    suspend fun getCollectionSortParams(collectionID: String): ApiResult<CollectionSortParams>

    suspend fun getAvailableCollections(fanficID: String): ApiResult<AvailableCollectionsModel>
    suspend fun addToCollection(collectionID: String, fanficID: String): Boolean
    suspend fun removeFromCollection(collectionID: String, fanficID: String): Boolean
}

class CollectionsApiImpl(
    private val client: OkHttpClient
): CollectionsApi {
    private val collectionsListParser = CollectionListParser()
    private val collectionSortParamsParser = CollectionSortParamsParser()

    override suspend fun getCollections(
        section: SectionWithQuery,
        page: Int
    ): ApiResult<ListResult<CollectionModel>> = coroutineScope {
        return@coroutineScope try {
            val response = client.get(
                url = ficbookUrl {
                    href(section.path)
                    section.queryParameters?.let { queryParams(it) }
                    page(page)
                }
            )
            val body = response.body.stringOrThrow()
            val document = Jsoup.parse(body)
            val collections = collectionsListParser.parse(document)
            val pageButtons = checkPageButtonsExists(document)
            ApiResult.success(
                ListResult(
                    list = collections,
                    hasNextPage = pageButtons.hasNext
                )
            )
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }

    override suspend fun getCollectionSortParams(
        collectionID: String
    ): ApiResult<CollectionSortParams> = coroutineScope {
        return@coroutineScope try {
            val response = client.get(
                url = ficbookUrl {
                    href("collections/$collectionID")
                }
            )
            val body: String = response.body.stringOrThrow()
            val document = Jsoup.parse(body)
            val result = collectionSortParamsParser.parse(document)
            ApiResult.success(result)
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }

    override suspend fun getAvailableCollections(
        fanficID: String
    ): ApiResult<AvailableCollectionsModel> = coroutineScope {
        return@coroutineScope try {
            val response = client.post(
                body = formBody {
                    add("fanficId", fanficID)
                },
                url = ficbookUrl {
                    href("ajax/collections/listforfanfic")
                }
            )
            val body: String = response.body.stringOrThrow()
            val model: AvailableCollectionsModel = Json.decodeFromString(body)
            ApiResult.success(model)
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }

    override suspend fun addToCollection(
        collectionID: String,
        fanficID: String
    ): Boolean = coroutineScope {
        return@coroutineScope try {
            val response = client.post(
                body = formBody {
                    add("collection_id", collectionID)
                    add("fanfic_id", fanficID)
                },
                url = ficbookUrl {
                    href("ajax/collections/addfanfic")
                }
            )
            val body = response.body.stringOrThrow()
            val result: AjaxSimpleResult = Json.decodeFromString(body)
            result.result
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun removeFromCollection(
        collectionID: String,
        fanficID: String
    ): Boolean = coroutineScope {
        return@coroutineScope try {
            val response = client.post(
                body = formBody {
                    add("collection_id", collectionID)
                    add("fanfic_id", fanficID)
                    add("action", "delete")
                },
                url = ficbookUrl {
                    href("ajax/collection")
                }
            )
            val body = response.body.stringOrThrow()
            val result: AjaxSimpleResult = Json.decodeFromString(body)
            result.result
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}