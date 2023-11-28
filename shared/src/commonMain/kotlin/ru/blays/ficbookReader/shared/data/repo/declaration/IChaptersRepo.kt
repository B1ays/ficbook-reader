package ru.blays.ficbookReader.shared.data.repo.declaration

import ru.blays.ficbookReader.shared.data.dto.FanficChapterStable
import ru.blays.ficbookapi.result.ApiResult

interface IChaptersRepo {
    suspend fun getChapter(href: String): ApiResult<String>

    suspend fun markAsReaded(chapters: FanficChapterStable): Boolean

    suspend fun saveReadProgress(href: String, charIndex: Int): Boolean
}