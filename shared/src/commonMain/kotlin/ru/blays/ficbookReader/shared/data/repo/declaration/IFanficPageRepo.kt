package ru.blays.ficbookReader.shared.data.repo.declaration

import ru.blays.ficbookReader.shared.data.dto.FanficPageModelStable
import ru.blays.ficbookapi.result.ApiResult

interface IFanficPageRepo {
    suspend fun get(href: String): ApiResult<FanficPageModelStable>

    suspend fun mark(mark: Boolean, fanficID: String): Boolean
    suspend fun follow(follow: Boolean, fanficID: String): Boolean
    suspend fun vote(vote: Boolean, partID: String): Boolean
    suspend fun read(read: Boolean, fanficID: String): Boolean
}