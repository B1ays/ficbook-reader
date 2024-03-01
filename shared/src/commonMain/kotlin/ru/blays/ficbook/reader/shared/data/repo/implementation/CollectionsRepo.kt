package ru.blays.ficbook.reader.shared.data.repo.implementation

import ru.blays.ficbook.api.api.CollectionsApi
import ru.blays.ficbook.api.data.SectionWithQuery
import ru.blays.ficbook.api.dataModels.CollectionModel
import ru.blays.ficbook.api.dataModels.ListResult
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.dto.AvailableCollectionsModel
import ru.blays.ficbook.reader.shared.data.dto.CollectionModelStable
import ru.blays.ficbook.reader.shared.data.dto.CollectionSortParamsStable
import ru.blays.ficbook.reader.shared.data.mappers.toStableModel
import ru.blays.ficbook.reader.shared.data.repo.declaration.ICollectionsRepo

class CollectionsRepo(
    private val api: CollectionsApi
): ICollectionsRepo {
    override suspend fun get(
        section: SectionWithQuery,
        page: Int
    ): ApiResult<ListResult<CollectionModelStable>> {
        return when(
            val result = api.getCollections(section, page)
        ) {
            is ApiResult.Error -> ApiResult.failure(result.exception)
            is ApiResult.Success -> ApiResult.success(
                ListResult(
                    list = result.value.list.map(CollectionModel::toStableModel),
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

    override suspend fun getCollectionSortParams(
        collectionID: String
    ): ApiResult<CollectionSortParamsStable> {
        return when(
            val result = api.getCollectionSortParams(collectionID)
        ) {
            is ApiResult.Error -> ApiResult.failure(result.exception)
            is ApiResult.Success -> ApiResult.success(
                result.value.toStableModel()
            )
        }
    }
}