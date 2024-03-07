package ru.blays.ficbook.reader.feature.copyImageFeature

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import coil3.Image
import coil3.annotation.ExperimentalCoilApi
import org.koin.mp.KoinPlatform.getKoin
import java.io.File

private const val FILE_PROVIDER_AUTHORITY = "ru.blays.ficbook.reader.provider"

@OptIn(ExperimentalCoilApi::class)
actual suspend fun copyImageToClipboard(image: Image): Boolean {
    val context: Context by getKoin().inject()

    val imageDrawable = image.asDrawable(context.resources)
    val bitmap = imageDrawable.toBitmap()

    val uri = context.saveImageToCache(bitmap) ?: return false

    val clipboard = context.getSystemService(ClipboardManager::class.java)

    val clip = ClipData.newUri(
        context.contentResolver,
        "cover",
        uri
    )
    clipboard.setPrimaryClip(clip)

    return true
}

private fun Context.saveImageToCache(bitmap: Bitmap): Uri? {
    val timestamp = System.currentTimeMillis()
    val file = File(cacheDir, "/clipboard_cache/clip-$timestamp.png").apply {
        if(!exists()) {
            parentFile?.mkdirs()
            createNewFile()
        }
        deleteOnExit()
    }
    val outputStream = file.outputStream()
    try {
        bitmap.compress(
            Bitmap.CompressFormat.PNG,
            100,
            file.outputStream()
        )
    } catch (e: Exception) {
        return null
    } finally {
        outputStream.close()
    }
    return FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, file)
}