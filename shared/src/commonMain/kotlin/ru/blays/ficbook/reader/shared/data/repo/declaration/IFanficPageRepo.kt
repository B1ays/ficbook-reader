package ru.blays.ficbook.reader.shared.data.repo.declaration

import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.dto.FanficPageModelStable

interface IFanficPageRepo {
    suspend fun get(href: String): ApiResult<FanficPageModelStable>

    suspend fun mark(mark: Boolean, fanficID: String): Boolean
    suspend fun follow(follow: Boolean, fanficID: String): Boolean
    suspend fun vote(vote: Boolean, partID: String): Boolean
    suspend fun read(read: Boolean, fanficID: String): Boolean
}