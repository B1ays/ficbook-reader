package ru.blays.ficbook.api.api

import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import ru.blays.ficbook.api.data.SectionWithQuery
import ru.blays.ficbook.api.dataModels.*
import ru.blays.ficbook.api.ficbookExtensions.ficbookUrl
import ru.blays.ficbook.api.json
import ru.blays.ficbook.api.okHttpDsl.*
import ru.blays.ficbook.api.parsers.CollectionListParser
import ru.blays.ficbook.api.parsers.CollectionMainInfoParser
import ru.blays.ficbook.api.parsers.CollectionPageParser
import ru.blays.ficbook.api.parsers.checkPageButtonsExists
import ru.blays.ficbook.api.result.ApiResult

interface CollectionsApi {
    suspend fun getCollections(section: SectionWithQuery, page: Int): ApiResult<ListResult<CollectionCardModel>>

    suspend fun getCollectionPage(collectionID: String): ApiResult<CollectionPageModel>
    suspend fun getMainInfo(collectionID: String): ApiResult<CollectionMainInfoModel>

    suspend fun getAvailableCollections(fanficID: String): ApiResult<AvailableCollectionsModel>
    suspend fun addToCollection(collectionID: String, fanficID: String): Boolean
    suspend fun removeFromCollection(collectionID: String, fanficID: String): Boolean

    suspend fun create(name: String, description: String, public: Boolean): ApiResult<Boolean>
    suspend fun update(collectionID: String, name: String, description: String, public: Boolean): ApiResult<Boolean>
    suspend fun delete(collectionID: String): ApiResult<Boolean>
    suspend fun follow(follow: Boolean, collectionID: String): ApiResult<Boolean>
}

internal class CollectionsApiImpl(
    private val client: OkHttpClient
): CollectionsApi {
    private val collectionsListParser = CollectionListParser()
    private val collectionPageParser = CollectionPageParser()
    private val collectionMainInfoParser = CollectionMainInfoParser()

    override suspend fun getCollections(
        section: SectionWithQuery,
        page: Int
    ): ApiResult<ListResult<CollectionCardModel>> = coroutineScope {
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

    override suspend fun getCollectionPage(
        collectionID: String
    ): ApiResult<CollectionPageModel> = coroutineScope {
        return@coroutineScope try {
            val response = client.get(
                url = ficbookUrl {
                    href("collections/$collectionID")
                }
            )
            val body: String = response.body.stringOrThrow()
            val document = Jsoup.parse(body)
            val result = collectionPageParser.parse(document)
            ApiResult.success(result)
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }

    override suspend fun getMainInfo(
        collectionID: String
    ): ApiResult<CollectionMainInfoModel> = coroutineScope {
        return@coroutineScope try {
            val response = client.get(
                url = ficbookUrl {
                    href("collections/$collectionID/edit")
                }
            )
            val body: String = response.body.stringOrThrow()
            val document = Jsoup.parse(body)
            val result = collectionMainInfoParser.parse(document)
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
            val model: AvailableCollectionsModel = json.decodeFromString(body)
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
            val result: AjaxSimpleResult = json.decodeFromString(body)
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
            val result: AjaxSimpleResult = json.decodeFromString(body)
            result.result
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun create(
        name: String,
        description: String,
        public: Boolean
    ): ApiResult<Boolean> = coroutineScope {
        return@coroutineScope try {
            val response = client.post(
                body = formBody {
                    add("name", name)
                    add("description", description)
                    add("is_public", if(public) "1" else "0")
                },
                url = ficbookUrl {
                    href("ajax/collections/create")
                }
            )
            ApiResult.success(response.isSuccessful)
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }

    override suspend fun update(
        collectionID: String,
        name: String,
        description: String,
        public: Boolean
    ): ApiResult<Boolean> = coroutineScope {
        return@coroutineScope try {
            val response = client.post(
                body = formBody {
                    add("collection_id", collectionID)
                    add("name", name)
                    add("description", description)
                    add("is_public", if(public) "1" else "0")
                },
                url = ficbookUrl {
                    href("ajax/collections/update")
                }
            )
            ApiResult.success(response.isSuccessful)
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }

    override suspend fun delete(
        collectionID: String
    ): ApiResult<Boolean> = coroutineScope {
        return@coroutineScope try {
            val response = client.post(
                body = formBody {
                    add("collection_id", collectionID)
                },
                url = ficbookUrl {
                    href("ajax/collection/delete")
                }
            )
            ApiResult.success(response.isSuccessful)
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }
    override suspend fun follow(
        follow: Boolean,
        collectionID: String
    ): ApiResult<Boolean> = coroutineScope {
        return@coroutineScope try {
            val href = if(follow) {
                "/ajax/collection/follow"
            } else {
                "/ajax/collection/unfollow"
            }
            val response = client.post(
                body = formBody {
                    add("collection_id", collectionID)
                },
                url = ficbookUrl {
                    href(href)
                }
            )
            val body = response.body.stringOrThrow()
            val result: AjaxSimpleResult = json.decodeFromString(body)
            ApiResult.success(result.result)
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }
}