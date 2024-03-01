package ru.blays.ficbook.reader.shared.data.repo.implementation

import ru.blays.ficbook.reader.shared.data.dto.FanficChapterStable
import ru.blays.ficbook.reader.shared.data.realm.entity.ChapterEntity
import ru.blays.ficbook.reader.shared.data.repo.declaration.IChaptersRepo
import ru.blays.ficbook.reader.shared.di.injectRealm
import ru.blays.ficbook.api.api.ChaptersApi
import ru.blays.ficbook.api.result.ApiResult

class ChaptersRepo(
    private val api: ChaptersApi
): IChaptersRepo {
    override suspend fun getChapterText(href: String): ApiResult<String> {
        return api.getChapterText(href)
    }

    override suspend fun markAsReaded(
        chapters: FanficChapterStable.SeparateChaptersModel.Chapter
    ): Boolean {
        val realm by injectRealm()

        return realm.write {
           val findedChapters = query(
               clazz = ChapterEntity::class,
               query = "href = $0", chapters.href
           ).first().find()

            if(findedChapters != null) {
                findedChapters.readed = true
                return@write true
            } else {
                return@write false
            }
        }
    }

    override suspend fun saveReadProgress(
        chapter: FanficChapterStable.SeparateChaptersModel.Chapter,
        fanficID: String,
        charIndex: Int
    ): Boolean {
        val realm by injectRealm()
        return realm.write {
            val savedChapter = query(
                clazz = ChapterEntity::class,
                query = "href = $0 AND fanficID = $1", chapter.href, fanficID
            ).first().find()

            if (savedChapter != null) {
                savedChapter.lastWatchedCharIndex = charIndex
                savedChapter.readed = true
                return@write true
            } else {
                copyToRealm(
                    ChapterEntity(
                        fanficID = fanficID,
                        name = chapter.name,
                        text = "",
                        href = chapter.href,
                        lastWatchedCharIndex = charIndex,
                        readed = true
                    )
                )
                return@write false
            }
        }
    }
}