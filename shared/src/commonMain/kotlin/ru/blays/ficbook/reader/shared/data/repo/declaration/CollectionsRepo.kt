package ru.blays.ficbook.reader.shared.data.repo.declaration

import ru.blays.ficbook.api.data.SectionWithQuery
import ru.blays.ficbook.api.dataModels.CollectionMainInfoModel
import ru.blays.ficbook.api.dataModels.ListResult
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.dto.AvailableCollectionsModel
import ru.blays.ficbook.reader.shared.data.dto.CollectionCardModelStable
import ru.blays.ficbook.reader.shared.data.dto.CollectionPageModelStable

interface ICollectionsRepo {
    suspend fun get(section: SectionWithQuery, page: Int): ApiResult<ListResult<CollectionCardModelStable>>

    suspend fun addToCollection(add: Boolean, collectionID: String, fanficID: String): Boolean

    suspend fun getAvailableCollections(fanficID: String): ApiResult<AvailableCollectionsModel>

    suspend fun getCollectionPage(collectionID: String): ApiResult<CollectionPageModelStable>
    suspend fun getMainInfo(collectionID: String): ApiResult<CollectionMainInfoModel>

    suspend fun create(name: String, description: String, public: Boolean): ApiResult<Boolean>
    suspend fun update(collectionID: String, name: String, description: String, public: Boolean): ApiResult<Boolean>
    suspend fun delete(collectionID: String): ApiResult<Boolean>
    suspend fun follow(follow: Boolean, collectionID: String): ApiResult<Boolean>
}