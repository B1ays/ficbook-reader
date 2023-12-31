package ru.blays.ficbookReader.shared.data.repo.implementation

import kotlinx.coroutines.coroutineScope
import ru.blays.ficbookReader.shared.data.dto.FanficChapterStable
import ru.blays.ficbookReader.shared.data.dto.FanficPageModelStable
import ru.blays.ficbookReader.shared.data.mappers.toStableModel
import ru.blays.ficbookReader.shared.data.realm.entity.ChapterEntity
import ru.blays.ficbookReader.shared.data.repo.declaration.IFanficPageRepo
import ru.blays.ficbookReader.shared.di.injectRealm
import ru.blays.ficbookapi.api.FanficPageApi
import ru.blays.ficbookapi.dataModels.FanficChapter
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
            is ApiResult.Success -> {
                val transformedChapters = when(
                    val chaptersHolder = result.value.chapters
                ) {
                    is FanficChapter.SeparateChaptersModel -> {
                        val associatedChapters = getSavedChapters(result.value.id)

                        val transformedChapters = chaptersHolder.chapters.map { separateChapter ->
                            val savedChapter = associatedChapters.firstOrNull { it.href == separateChapter.href }
                            FanficChapterStable.SeparateChaptersModel.Chapter(
                                chapterID = separateChapter.chapterID,
                                href = separateChapter.href,
                                name = separateChapter.name,
                                date = separateChapter.date,
                                commentsCount = separateChapter.commentsCount,
                                lastWatchedCharIndex = savedChapter?.lastWatchedCharIndex ?: 0,
                                readed = savedChapter?.readed ?: false
                            )
                        }

                        FanficChapterStable.SeparateChaptersModel(
                            chapters = transformedChapters,
                            chaptersCount = chaptersHolder.chaptersCount
                        )
                    }
                    is FanficChapter.SingleChapterModel -> {
                        chaptersHolder.toStableModel()
                    }
                }
                ApiResult.success(
                    result.value.toStableModel(transformedChapters)
                )
            }
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

    private suspend fun getSavedChapters(
        fanficID: String
    ): List<ChapterEntity> = coroutineScope {
        val realm by injectRealm()
        return@coroutineScope realm.query(
            clazz = ChapterEntity::class,
            query = "fanficID = $0", fanficID
        ).find()
    }
}