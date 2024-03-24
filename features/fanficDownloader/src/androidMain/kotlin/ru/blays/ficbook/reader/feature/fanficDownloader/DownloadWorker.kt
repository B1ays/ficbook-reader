package ru.blays.ficbook.reader.feature.fanficDownloader

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.documentnode.epub4j.domain.Author
import io.documentnode.epub4j.domain.Book
import io.documentnode.epub4j.domain.Resource
import io.documentnode.epub4j.epub.EpubWriter
import kotlinx.coroutines.delay
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbook.api.UrlProcessor.getUrlForHref
import ru.blays.ficbook.api.api.ChaptersApi
import ru.blays.ficbook.api.api.FanficPageApi
import ru.blays.ficbook.api.dataModels.FanficChapter
import ru.blays.ficbook.api.result.ResponseResult
import ru.blays.ficbook.features.fanficDownloader.R
import kotlin.time.Duration.Companion.seconds

class DownloadWorker(
    context: Context,
    parameters: WorkerParameters
) : CoroutineWorker(context, parameters) {
    @Suppress("PrivatePropertyName")
    private val TAG = this::class.simpleName

    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    private var notificationID = -1

    private var maxProgress = 0

    private val cancel = applicationContext.resources.getString(android.R.string.cancel)
    // This PendingIntent can be used to cancel the worker
    private val cancelIntent = WorkManager
        .getInstance(applicationContext)
        .createCancelPendingIntent(id)

    private var openFileIntent: PendingIntent? = null

    override suspend fun doWork(): Result {
        val uriString = inputData.getString(URI_KEY) ?: return Result.failure()
        val uri = uriString.toUri()
        val fanficId = inputData.getString(FANFIC_ID_KEY) ?: return Result.failure()
        val title = inputData.getString(TITLE_KEY) ?: return Result.failure()
        val format = inputData.getString(FORMAT_KEY) ?: return Result.failure()

        notificationID = id.hashCode()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = uri
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        openFileIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val foregroundInfo = createForegroundInfo(
            title = title,
            content = applicationContext.getString(R.string.download_in_progress)
        )
        setForeground(foregroundInfo)

        val success = if(format.equals(FORMAT_EPUB, true)) {
            downloadFanficInEpub(
                file = uri,
                fanficId = fanficId,
                title = title,
            )
        } else if(format.equals(FORMAT_TXT, true)) {
            downloadFanficInTxt(
                file = uri,
                fanficId = fanficId,
                title = title,
            )
        } else {
            false
        }

        return if(success) {
            Result.success()
        } else {
            Result.failure()
        }
    }

    private suspend fun downloadFanficInEpub(
        file: Uri,
        fanficId: String,
        title: String,
    ): Boolean {
        val fanficsApi: FanficPageApi by getKoin().inject()
        val chaptersApi: ChaptersApi by getKoin().inject()

        val chaptersHtmls: MutableList<Pair<String, String>> = mutableListOf()

        val fanfic = fanficsApi.getById(fanficId)
            .getOrNull() 
            ?: return false

        when(
            val chapters = fanfic.chapters
        ) {
            is FanficChapter.SeparateChaptersModel -> {
                maxProgress = chapters.chapters.size
                for ((index, chapter) in chapters.chapters.withIndex()) {
                    while(true) {
                        createNotification(
                            progress = index+1,
                            title = title,
                            content = applicationContext.getString(R.string.download_chapter, chapter.name)
                        ).let {
                            notificationManager.notify(notificationID, it)
                        }
                        when (
                            val result = chaptersApi.getChapterHtml(fanficId, chapter.chapterID)
                        ) {
                            is ResponseResult.Error -> {
                                Log.w(TAG, "Request failure with code: ${result.code}")
                                createNotification(
                                    title = title,
                                    content = applicationContext.getString(R.string.download_chapter_error)
                                ).let {
                                    notificationManager.notify(notificationID, it)
                                }
                                delay(25.seconds)
                                continue
                            }
                            is ResponseResult.Success -> {
                                val html = result.value
                                chaptersHtmls += chapter.name to html
                                delay(4.seconds)
                                break
                            }
                        }
                    }
                }
            }
            is FanficChapter.SingleChapterModel -> {
                maxProgress = 1
                val processedText = chapters.text.replace("\n", "<br/>")
                chaptersHtmls += applicationContext.getString(R.string.chapter_default_name) to processedText
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
            chaptersCount = chaptersHtmls.size,
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

        chaptersHtmls.forEachIndexed { index, (chapterTitle, textHtml) ->
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

        val outputStream = applicationContext.contentResolver.openOutputStream(file)

        @Suppress("BlockingMethodInNonBlockingContext")
        return if (outputStream != null) {
            try {
                writer.write(book, outputStream)

                createSuccessNotification(title).let { notification ->
                    notificationManager.notify(
                        notificationID + 1,
                        notification
                    )
                }

                true
            } catch (e: Exception) {
                Log.e(TAG, "Error while writing epub file", e)
                false
            } finally {
                outputStream.close()
            }
        } else {
            false
        }
    }

    private suspend fun downloadFanficInTxt(
        file: Uri,
        fanficId: String,
        title: String,
    ): Boolean {
        val fanficsApi: FanficPageApi by getKoin().inject()
        val api: ChaptersApi by getKoin().inject()

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
                maxProgress = chapters.chapters.size
                for ((index, chapter) in chapters.chapters.withIndex()) {
                    while(true) {
                        createNotification(
                            progress = index+1,
                            title = title,
                            content = applicationContext.getString(R.string.download_chapter, chapter.name)
                        ).let {
                            notificationManager.notify(notificationID, it)
                        }
                        when (
                            val result = api.getChapterText(fanficId, chapter.chapterID)
                        ) {
                            is ResponseResult.Error -> {
                                Log.w(TAG, "Request failure with code: ${result.code}")
                                createNotification(
                                    title = title,
                                    content = applicationContext.getString(R.string.download_chapter_error)
                                ).let {
                                    notificationManager.notify(notificationID, it)
                                }
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
                maxProgress = 1
                fullText += "\n\n${chapters.text}"
            }
        }

        val outputStream = applicationContext.contentResolver.openOutputStream(file)

        @Suppress("BlockingMethodInNonBlockingContext")
        return if(outputStream != null) {
            try {
                outputStream.writer().write(fullText)

                createSuccessNotification(title).let { notification ->
                    notificationManager.notify(
                        notificationID + 1,
                        notification
                    )
                }

                true
            } catch (e: Exception) {
                Log.e(TAG, "Error while writing txt file", e)
                false
            } finally {
                outputStream.close()
            }
        } else {
            false
        }
    }

    private fun createForegroundInfo(
        title: String,
        content: String
    ): ForegroundInfo {
        createChannel()

        val notification = createNotification(
            title = title,
            content = content
        )

        return ForegroundInfo(notificationID, notification)
    }

    private fun createNotification(
        progress: Int? = null,
        title: String,
        content: String
    ): Notification {
        return NotificationCompat.Builder(
            applicationContext,
            NOTIFICATION_CHANNEL_ID
        ).apply {
            setContentTitle(title)
            setTicker(title)
            setContentText(content)
            setSmallIcon(R.drawable.ic_download)
            setOnlyAlertOnce(true)
            setOngoing(true)
            // Add the cancel action to the notification which can
            // be used to cancel the worker
            addAction(android.R.drawable.ic_delete, cancel, cancelIntent)
            if (progress != null) {
                setProgress(maxProgress, progress, false)
            }
        }.build()
    }

    private fun createSuccessNotification(
        title: String
    ): Notification {
        return NotificationCompat.Builder(
            applicationContext,
            NOTIFICATION_CHANNEL_ID
        ).apply {
            setContentTitle(title)
            setTicker(title)
            setContentText(applicationContext.getString(R.string.download_complete))
            setSmallIcon(R.drawable.ic_download)
            setOnlyAlertOnce(true)
            setAutoCancel(true)
            setContentIntent(openFileIntent)
        }.build()
    }


    private fun createChannel() {
        notificationManager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                applicationContext.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
        )
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "FanficDownloader"

        internal const val FANFIC_ID_KEY = "FANFIC_ID"
        internal const val URI_KEY = "URI"
        internal const val TITLE_KEY = "TITLE"
        internal const val FORMAT_KEY = "FORMAT"

        private const val FORMAT_EPUB = "epub"
        private const val FORMAT_TXT = "txt"
    }
}