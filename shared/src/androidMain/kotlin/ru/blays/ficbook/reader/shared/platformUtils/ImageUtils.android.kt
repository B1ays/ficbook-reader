package ru.blays.ficbook.reader.shared.platformUtils

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import coil3.SingletonImageLoader
import coil3.asDrawable
import coil3.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.koin.mp.KoinPlatform.getKoin
import java.io.File

actual suspend fun downloadImageToFile(
    url: String,
    file: File,
    formatName: String
): Boolean = coroutineScope {
    return@coroutineScope try {
        val context: Context by getKoin().inject()
        val resources = context.resources
        val image = SingletonImageLoader.get(context)
            .execute(
                request = ImageRequest.Builder(context)
                    .data(url)
                    .build()
            ).image
            ?: return@coroutineScope false
        val bitmap = image.asDrawable(resources).toBitmap()
        withContext(Dispatchers.IO) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, file.outputStream())
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}