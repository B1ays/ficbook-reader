package ru.blays.ficbook.reader.feature.fileDownload

import android.content.Context
import android.util.Log
import com.darkrockstudios.libraries.mpfilepicker.PlatformFile
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbook.reader.feature.fileDownload.makeDownloadResponse

private const val TAG = "FileDownloader"

@Suppress("BlockingMethodInNonBlockingContext")
actual suspend fun downloadMPFile(url: String, file: PlatformFile) = coroutineScope {
    val uri = file.uri
    val context: Context by getKoin().inject()
    val notificationUtils: NotificationUtils by getKoin().inject()

    val responseResult = makeDownloadResponse(url)
    if(responseResult == null) {
        Log.w(TAG, "Response result is null")
        return@coroutineScope
    }

    val inputStream = responseResult.inputStream
    val outputStream = context.contentResolver.openOutputStream(uri)
    if(outputStream == null) {
        Log.w(TAG, "Output stream is null")
        return@coroutineScope
    }

    var progress: Int = 0
    var originalFileSize: Long = responseResult.contentLength
    var bytesCopied: Long = 0

    if(originalFileSize == -1L) {
        originalFileSize = 1
    }

    launch {
        notificationUtils.createDownloadNotification(
            uri = uri,
            contentTitle = "Загрузка файла"
        ) {
            progress
        }
    }

    inputStream.use {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var bytes = it.read(buffer)
        while (bytes >= 0) {
            outputStream.write(buffer, 0, bytes)
            bytesCopied += bytes

            Log.d(TAG, "Bytes copied: $bytesCopied")

            progress = ((bytesCopied / originalFileSize.toFloat()) * 100).toInt().coerceIn(0, 100)

            bytes = it.read(buffer)
        }
    }

    Log.d(TAG, "Download complete")

    outputStream.close()
}