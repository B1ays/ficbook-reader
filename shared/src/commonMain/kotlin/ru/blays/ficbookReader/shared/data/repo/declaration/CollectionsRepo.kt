package ru.blays.ficbookReader.shared.data.repo.declaration

import ru.blays.ficbookReader.shared.data.dto.AvailableCollectionsModel
import ru.blays.ficbookReader.shared.data.dto.CollectionModelStable
import ru.blays.ficbookapi.data.SectionWithQuery
import ru.blays.ficbookapi.dataModels.ListResult
import ru.blays.ficbookapi.result.ApiResult

interface ICollectionsRepo {
    suspend fun get(section: SectionWithQuery, page: Int): ApiResult<ListResult<CollectionModelStable>>

    suspend fun addToCollection(add: Boolean, collectionID: String, fanficID: String): Boolean

    suspend fun getAvailableCollections(fanficID: String): ApiResult<AvailableCollectionsModel>
}