package ru.blays.ficbook.reader.feature.fanficDownloader

import android.content.Context
import androidx.work.*
import com.darkrockstudios.libraries.mpfilepicker.PlatformFile
import org.koin.mp.KoinPlatform.getKoin

actual suspend fun downloadFanficInEpub(
    file: PlatformFile,
    task: DownloadTask
): Boolean {
    val context: Context by getKoin().inject()

    val workManager = WorkManager.getInstance(context)

    val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>().apply {
        setInputData(
            workDataOf(
                DownloadWorker.URI_KEY to file.uri.toString(),
                DownloadWorker.FANFIC_ID_KEY to task.fanficID,
                DownloadWorker.TITLE_KEY to task.title,
                DownloadWorker.FORMAT_KEY to task.format
            )
        )
        setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
    }.build()

    workManager.enqueue(workRequest)
    return true

}