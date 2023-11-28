package ru.blays.ficbookReader.shared.data.repo.implementation

import ru.blays.ficbookReader.shared.data.dto.FanficPageModelStable
import ru.blays.ficbookReader.shared.data.mappers.toStableModel
import ru.blays.ficbookReader.shared.data.repo.declaration.IFanficPageRepo
import ru.blays.ficbookapi.api.FanficPageApi
import ru.blays.ficbookapi.result.ApiResult

class FanficPageRepo(
    private val api: FanficPageApi
): IFanficPageRepo {
    override suspend fun get(
        href: String
    ): ApiResult<FanficPageModelStable> {
        return when(
            val result = api.get(href)
        ) {
            is ApiResult.Error -> ApiResult.failure(result.exception)
            is ApiResult.Success -> ApiResult.success(result.value.toStableModel())
        }
    }

    override suspend fun mark(
        mark: Boolean,
        fanficID: String
    ): Boolean {
        return api.mark(mark, fanficID)
    }

    override suspend fun follow(
        follow: Boolean,
        fanficID: String
    ): Boolean {
        return api.follow(follow, fanficID)
    }

    override suspend fun vote(
        vote: Boolean,
        partID: String
    ): Boolean {
        return api.vote(vote, partID)
    }

    override suspend fun read(
        read: Boolean,
        fanficID: String
    ): Boolean {
        return api.read(read, fanficID)
    }
}