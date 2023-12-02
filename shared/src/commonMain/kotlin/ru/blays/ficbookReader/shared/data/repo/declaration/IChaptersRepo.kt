package ru.blays.ficbookReader.shared.data.repo.declaration

import ru.blays.ficbookReader.shared.data.dto.FanficChapterStable
import ru.blays.ficbookapi.result.ApiResult

interface IChaptersRepo {
    suspend fun getChapterText(href: String): ApiResult<String>

    suspend fun markAsReaded(chapters: FanficChapterStable.SeparateChaptersModel.Chapter): Boolean

    suspend fun saveReadProgress(chapter: FanficChapterStable.SeparateChaptersModel.Chapter, fanficID: String, charIndex: Int): Boolean
}