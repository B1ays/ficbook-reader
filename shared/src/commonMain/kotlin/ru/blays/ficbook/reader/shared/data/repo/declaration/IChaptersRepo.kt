package ru.blays.ficbook.reader.shared.data.repo.declaration

import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.dto.FanficChapterStable

interface IChaptersRepo {
    suspend fun getChapterText(href: String): ApiResult<String>

    suspend fun markAsReaded(chapters: FanficChapterStable.SeparateChaptersModel.Chapter): Boolean

    suspend fun saveReadProgress(chapter: FanficChapterStable.SeparateChaptersModel.Chapter, fanficID: String, charIndex: Int): Boolean
}