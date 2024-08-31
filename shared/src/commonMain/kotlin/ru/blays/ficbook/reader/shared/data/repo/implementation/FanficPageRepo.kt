package ru.blays.ficbook.reader.shared.data.repo.implementation

import kotlinx.coroutines.coroutineScope
import org.koin.core.component.KoinComponent
import ru.blays.ficbook.reader.shared.data.dto.FanficChapterStable
import ru.blays.ficbook.reader.shared.data.dto.FanficPageModelStable
import ru.blays.ficbook.reader.shared.data.mappers.toStableModel
import ru.blays.ficbook.reader.shared.data.realm.entity.ChapterEntity
import ru.blays.ficbook.reader.shared.data.repo.declaration.IFanficPageRepo
import ru.blays.ficbook.reader.shared.di.injectRealm
import ru.blays.ficbook.api.api.FanficPageApi
import ru.blays.ficbook.api.dataModels.FanficChapter
import ru.blays.ficbook.api.result.ApiResult

class FanficPageRepo(
    private val api: FanficPageApi
): IFanficPageRepo, KoinComponent {
    private val realm by injectRealm()

    override suspend fun get(
        href: String
    ): ApiResult<FanficPageModelStable> {
        return when(
            val result = api.getByHref(href)
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
        return@coroutineScope realm.query(
            clazz = ChapterEntity::class,
            query = "fanficID = $0", fanficID
        ).find()
    }
}