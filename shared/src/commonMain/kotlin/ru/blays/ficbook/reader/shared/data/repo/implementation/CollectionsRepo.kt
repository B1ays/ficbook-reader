package ru.blays.ficbook.reader.shared.data.repo.implementation

import ru.blays.ficbook.api.api.CollectionsApi
import ru.blays.ficbook.api.data.SectionWithQuery
import ru.blays.ficbook.api.dataModels.CollectionCardModel
import ru.blays.ficbook.api.dataModels.CollectionMainInfoModel
import ru.blays.ficbook.api.dataModels.ListResult
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.dto.AvailableCollectionsModel
import ru.blays.ficbook.reader.shared.data.dto.CollectionCardModelStable
import ru.blays.ficbook.reader.shared.data.dto.CollectionPageModelStable
import ru.blays.ficbook.reader.shared.data.mappers.toStableModel
import ru.blays.ficbook.reader.shared.data.repo.declaration.ICollectionsRepo

class CollectionsRepo(
    private val api: CollectionsApi
): ICollectionsRepo {
    override suspend fun get(
        section: SectionWithQuery,
        page: Int
    ): ApiResult<ListResult<CollectionCardModelStable>> {
        return when(
            val result = api.getCollections(section, page)
        ) {
            is ApiResult.Error -> ApiResult.failure(result.exception)
            is ApiResult.Success -> ApiResult.success(
                ListResult(
                    list = result.value.list.map(CollectionCardModel::toStableModel),
                    hasNextPage = result.value.hasNextPage
                )
            )
        }
    }

    override suspend fun addToCollection(
        add: Boolean,
        collectionID: String,
        fanficID: String
    ): Boolean {
        return if(add) {
            api.addToCollection(collectionID, fanficID)
        } else {
            api.removeFromCollection(collectionID, fanficID)
        }
    }

    override suspend fun getAvailableCollections(
        fanficID: String
    ): ApiResult<AvailableCollectionsModel> {
        return when(
            val result = api.getAvailableCollections(fanficID)
        ) {
            is ApiResult.Error -> ApiResult.failure(result.exception)
            is ApiResult.Success -> ApiResult.success(
                result.value.toStableModel()
            )
        }
    }

    override suspend fun getCollectionPage(
        collectionID: String
    ): ApiResult<CollectionPageModelStable> {
        return when(
            val result = api.getCollectionPage(collectionID)
        ) {
            is ApiResult.Error -> ApiResult.failure(result.exception)
            is ApiResult.Success -> ApiResult.success(
                result.value.toStableModel()
            )
        }
    }

    override suspend fun getMainInfo(collectionID: String): ApiResult<CollectionMainInfoModel> {
        return api.getMainInfo(collectionID)
    }

    override suspend fun create(name: String, description: String, public: Boolean): ApiResult<Boolean> {
        return api.create(name, description, public)
    }

    override suspend fun update(
        collectionID: String,
        name: String,
        description: String,
        public: Boolean
    ): ApiResult<Boolean> {
        return api.update(collectionID, name, description, public)
    }

    override suspend fun delete(collectionID: String): ApiResult<Boolean> {
        return api.delete(collectionID)
    }

    override suspend fun follow(follow: Boolean, collectionID: String): ApiResult<Boolean> {
        return api.follow(follow, collectionID)
    }
}