package ru.blays.ficbook.reader.shared.data.repo.implementation

import io.realm.kotlin.Realm
import ru.blays.ficbook.api.api.ChaptersApi
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.api.result.ResponseResult
import ru.blays.ficbook.reader.shared.data.dto.FanficChapterStable
import ru.blays.ficbook.reader.shared.data.realm.entity.ChapterEntity
import ru.blays.ficbook.reader.shared.data.repo.declaration.IChaptersRepo

class ChaptersRepo(
    private val api: ChaptersApi,
    private val realm: Realm
): IChaptersRepo {
    override suspend fun getChapterText(href: String): ApiResult<String> {
        return api.getChapterText(href)
    }

    override suspend fun getChapterHtml(fanficID: String, id: String): ResponseResult<String> {
        return api.getChapterHtml(fanficID, id)
    }
    override suspend fun getChapterHtml(href: String): ResponseResult<String> {
        return api.getChapterHtml(href)
    }

    override suspend fun markAsReaded(
        chapters: FanficChapterStable.SeparateChaptersModel.Chapter
    ): Boolean {
        return realm.write {
            val foundedChapters = query(ChapterEntity::class, "href = $0", chapters.href)
               .first()
               .find()

            if (foundedChapters != null) {
                foundedChapters.readed = true
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