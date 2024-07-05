package ru.blays.ficbook.reader.shared.platformUtils

import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.request.ImageRequest
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.jetbrains.skiko.toBufferedImage
import java.io.File
import javax.imageio.ImageIO

@OptIn(ExperimentalCoilApi::class)
actual suspend fun downloadImageToFile(
    url: String,
    file: File,
    formatName: String
): Boolean = coroutineScope {
    return@coroutineScope try {
        val platformContext = PlatformContext.INSTANCE
        val image = SingletonImageLoader.get(platformContext)
            .execute(
                request = ImageRequest.Builder(platformContext)
                    .data(url)
                    .build()
            ).image
            ?: return@coroutineScope false
        val bufferedImage = image.toBitmap().toBufferedImage()
        withContext(Dispatchers.IO) {
            ImageIO.write(bufferedImage, formatName, file)
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}