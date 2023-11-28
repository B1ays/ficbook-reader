package ru.blays.ficbookReader.shared.data.repo.implementation

import ru.blays.ficbookReader.shared.data.dto.FanficChapterStable
import ru.blays.ficbookReader.shared.data.realm.entity.ChapterEntity
import ru.blays.ficbookReader.shared.data.repo.declaration.IChaptersRepo
import ru.blays.ficbookReader.shared.di.injectRealm
import ru.blays.ficbookapi.api.ChaptersApi
import ru.blays.ficbookapi.result.ApiResult

class ChaptersRepo(
    private val api: ChaptersApi
): IChaptersRepo {
    override suspend fun getChapter(href: String): ApiResult<String> {
        return api.getChapterText(href)
    }

    override suspend fun markAsReaded(chapters: FanficChapterStable): Boolean {
        return false // TODO
    }

    override suspend fun saveReadProgress(href: String, charIndex: Int): Boolean {
        val realm = injectRealm(ChapterEntity::class)
        return realm.write {
            val savedChapter = query(ChapterEntity::class, "href = $0", href)
                .first()
                .find()

            if (savedChapter != null) {
                savedChapter.lastWatchedCharIndex = charIndex
                return@write true
            } else {
                return@write false
            }
        }
    }
}