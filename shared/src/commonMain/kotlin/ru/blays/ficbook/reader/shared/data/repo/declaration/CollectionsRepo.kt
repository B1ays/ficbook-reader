package ru.blays.ficbook.reader.shared.data.repo.declaration

import ru.blays.ficbook.api.data.SectionWithQuery
import ru.blays.ficbook.api.dataModels.ListResult
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.dto.AvailableCollectionsModel
import ru.blays.ficbook.reader.shared.data.dto.CollectionModelStable
import ru.blays.ficbook.reader.shared.data.dto.CollectionSortParamsStable

interface ICollectionsRepo {
    suspend fun get(section: SectionWithQuery, page: Int): ApiResult<ListResult<CollectionModelStable>>

    suspend fun addToCollection(add: Boolean, collectionID: String, fanficID: String): Boolean

    suspend fun getAvailableCollections(fanficID: String): ApiResult<AvailableCollectionsModel>

    suspend fun getCollectionSortParams(collectionID: String): ApiResult<CollectionSortParamsStable>
}