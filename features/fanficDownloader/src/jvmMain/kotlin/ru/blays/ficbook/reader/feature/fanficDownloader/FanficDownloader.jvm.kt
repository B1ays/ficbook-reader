package ru.blays.ficbook.reader.feature.fanficDownloader

import com.darkrockstudios.libraries.mpfilepicker.PlatformFile
import io.documentnode.epub4j.domain.Author
import io.documentnode.epub4j.domain.Book
import io.documentnode.epub4j.domain.Resource
import io.documentnode.epub4j.epub.EpubWriter
import kotlinx.coroutines.delay
import org.koin.mp.KoinPlatform
import ru.blays.ficbook.api.UrlProcessor.getUrlForHref
import ru.blays.ficbook.api.api.ChaptersApi
import ru.blays.ficbook.api.api.FanficPageApi
import ru.blays.ficbook.api.dataModels.FanficChapter
import ru.blays.ficbook.api.result.ResponseResult
import java.io.File
import kotlin.time.Duration.Companion.seconds

actual suspend fun downloadFanficInEpub(
    file: PlatformFile,
    task: DownloadTask
): Boolean = if(
    task.format.equals("epub", true)
) {
    downloadFanficInEpub(
        file = file.file,
        fanficId = task.fanficID,
        title = task.title
    )
} else if(
    task.format.equals("txt", true)
) {
    downloadFanficInTxt(
        file = file.file,
        fanficId = task.fanficID,
        title = task.title
    )
} else {
    false
}


private suspend fun downloadFanficInEpub(
    file: File,
    fanficId: String,
    title: String,
): Boolean {
    val fanficsApi: FanficPageApi by KoinPlatform.getKoin().inject()
    val chaptersApi: ChaptersApi by KoinPlatform.getKoin().inject()

    val chaptersHtml: MutableList<Pair<String, String>> = mutableListOf()

    val fanfic = fanficsApi.getById(fanficId)
        .getOrNull()
        ?: return false

    when(
        val chapters = fanfic.chapters
    ) {
        is FanficChapter.SeparateChaptersModel -> {
            for (chapter in chapters.chapters) {
                while(true) {
                    when (
                        val result = chaptersApi.getChapterHtml(fanficId, chapter.chapterID)
                    ) {
                        is ResponseResult.Error -> {
                            delay(25.seconds)
                            continue
                        }
                        is ResponseResult.Success -> {
                            val html = result.value
                            chaptersHtml += chapter.name to html
                            delay(4.seconds)
                            break
                        }
                    }
                }
            }
        }
        is FanficChapter.SingleChapterModel -> {
            val processedText = chapters.text.replace("\n", "<br/>")
            chaptersHtml += "Глава 1" to processedText
        }
    }

    val book = Book()

    book.metadata.apply {
        addTitle(title)

        fanfic.authors.map { it.user.name }
            .forEach { author ->
                addAuthor(
                    author = Author(
                        firstname = author
                    )
                )
            }

        addDescription(fanfic.description)
    }

    val titleHtml = generateTitlePage(
        title = fanfic.name,
        href = getUrlForHref("readfic/${fanfic.id}"),
        direction = fanfic.status.direction.direction,
        authors = fanfic.authors.map { it.user.name },
        fandoms = fanfic.fandoms.map { it.name },
        rating = fanfic.status.rating.rating,
        chaptersCount = chaptersHtml.size,
        status = fanfic.status.status.status,
        tags = fanfic.tags.map { it.name },
        description = fanfic.description,
        publicationRules = fanfic.publicationRules,
        authorComment = fanfic.authorComment
    )

    book.addSection(
        title = null,
        resource = Resource(
            id = "title",
            data = titleHtml.toByteArray(),
            href = "title.xhtml"
        )
    )

    chaptersHtml.forEachIndexed { index, (chapterTitle, textHtml) ->
        val fullHtml = createHtmlPageForContent(
            title = chapterTitle,
            content = textHtml
        )

        book.addSection(
            title = chapterTitle,
            resource = Resource(
                id = "chapter$index",
                data = fullHtml.toByteArray(),
                href = "chapter$index.xhtml"
            )
        )
    }

    val writer = EpubWriter()

    val outputStream = file.outputStream()

    @Suppress("BlockingMethodInNonBlockingContext")
    return try {
        writer.write(book, outputStream)
        true
    } catch (e: Exception) {
        false
    } finally {
        outputStream.close()
    }
}

private suspend fun downloadFanficInTxt(
    file: File,
    fanficId: String,
    title: String,
): Boolean {
    val fanficsApi: FanficPageApi by KoinPlatform.getKoin().inject()
    val api: ChaptersApi by KoinPlatform.getKoin().inject()

    val fanfic = fanficsApi.getById(fanficId)
        .getOrNull()
        ?: return false

    var fullText = generateDescription(
        title = fanfic.name,
        link = getUrlForHref("readfic/${fanfic.id}"),
        direction = fanfic.status.direction.direction,
        authors = fanfic.authors.map { it.user.name },
        fandoms = fanfic.fandoms.map { it.name },
        rating = fanfic.status.rating.rating,
        chaptersCount = fanfic.chapters.size,
        status = fanfic.status.status.status,
        tags = fanfic.tags.map { it.name },
        description = fanfic.description,
        publicationRules = fanfic.publicationRules,
        authorComment = fanfic.authorComment
    )

    when(
        val chapters = fanfic.chapters
    ) {
        is FanficChapter.SeparateChaptersModel -> {
            for (chapter in chapters.chapters) {
                while(true) {
                    when (
                        val result = api.getChapterText(fanficId, chapter.chapterID)
                    ) {
                        is ResponseResult.Error -> {
                            delay(25.seconds)
                            continue
                        }
                        is ResponseResult.Success -> {
                            val text = result.value
                            fullText += addTitleForChapter(
                                text = text,
                                title = chapter.name
                            )
                            delay(4.seconds)
                            break
                        }
                    }
                }
            }
        }
        is FanficChapter.SingleChapterModel -> {
            fullText += "\n\n${chapters.text}"
        }
    }

    val outputStream = file.outputStream()

    return try {
        outputStream.writer().write(fullText)
        true
    } catch (e: Exception) {
        false
    } finally {
        outputStream.close()
    }
}
