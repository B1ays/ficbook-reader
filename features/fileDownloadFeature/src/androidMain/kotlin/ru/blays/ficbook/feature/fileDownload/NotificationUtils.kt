package ru.blays.ficbook.feature.fileDownload

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.delay
import ru.blays.ficbook.features.fileDownloadFeature.R
import kotlin.time.Duration.Companion.seconds


internal class NotificationUtils(private val context: Context) {
    private fun createNotificationChannel() {
        val name = "Загрузка файла"
        val descriptionText = "Уведомление о загрузке файла"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system.
        val notificationManager: NotificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    suspend fun createDownloadNotification(
        uri: Uri,
        contentTitle: String,
        progressSource: () -> Int
    ) {
        val contentResolver = context.contentResolver

        val fileName = queryName(contentResolver, uri)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setContentTitle(contentTitle)
            setContentText(fileName)
            setSmallIcon(R.drawable.ic_download)
            setOnlyAlertOnce(true)
            setOngoing(true)
            priority = NotificationCompat.PRIORITY_LOW
        }
        val id = (contentTitle + fileName).hashCode()

        if (
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        } else {
            NotificationManagerCompat.from(context).run {
                builder.setProgress(MAX_PROGRESS, 0, false)
                notify(id, builder.build())

                var progress = progressSource()

                while(progress != MAX_PROGRESS) {
                    progress = progressSource()
                    builder.setProgress(MAX_PROGRESS, progress, false)
                    notify(id, builder.build())
                    delay(1.seconds)
                }

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = uri
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val pendingIntent: PendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
                builder.setContentText("Загрузка завершена")
                builder.setProgress(0, 0, false)
                builder.setOngoing(false)
                builder.setContentIntent(pendingIntent)
                notify(id, builder.build())
            }
        }
    }

    private fun queryName(resolver: ContentResolver, uri: Uri): String? {
        val returnCursor = resolver.query(uri, null, null, null, null) ?: return null
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }

    init {
        createNotificationChannel()
    }

    companion object {
        private const val MAX_PROGRESS = 100
        const val CHANNEL_ID = "fileDownload"
    }
}