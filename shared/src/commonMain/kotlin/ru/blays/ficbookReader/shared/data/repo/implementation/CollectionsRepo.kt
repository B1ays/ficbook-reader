package ru.blays.ficbookReader.shared.data.repo.implementation

import ru.blays.ficbookReader.shared.data.dto.AvailableCollectionsModel
import ru.blays.ficbookReader.shared.data.dto.CollectionModelStable
import ru.blays.ficbookReader.shared.data.mappers.toStableModel
import ru.blays.ficbookReader.shared.data.repo.declaration.ICollectionsRepo
import ru.blays.ficbookapi.api.CollectionsApi
import ru.blays.ficbookapi.data.SectionWithQuery
import ru.blays.ficbookapi.dataModels.CollectionModel
import ru.blays.ficbookapi.dataModels.ListResult
import ru.blays.ficbookapi.result.ApiResult

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
}