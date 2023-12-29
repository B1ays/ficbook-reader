package ru.blays.ficbookReader.platformUtils

import coil3.ComponentRegistry
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.annotation.ExperimentalCoilApi
import coil3.disk.DiskCache
import coil3.fetch.NetworkFetcher
import coil3.memory.MemoryCache
import coil3.request.crossfade
import coil3.util.DebugLogger
import okio.FileSystem

@OptIn(ExperimentalCoilApi::class)
fun createImageLoader(
    context: PlatformContext,
    debug: Boolean = false,
): ImageLoader {
    return ImageLoader.Builder(context)
        .components {
            add(NetworkFetcher.Factory())
            addPlatformComponents()
        }
        .memoryCache {
            MemoryCache.Builder()
                // Set the max size to 25% of the app's available memory.
                .maxSizePercent(context, percent = 0.25)
                .build()
        }
        .diskCache(::newDiskCache)
        // Show a short crossfade when loading images asynchronously.
        .crossfade(true)
        // Enable logging if this is a debug build.
        .apply {
            if (debug) {
                logger(DebugLogger())
            }
        }
        .build()
}

internal fun newDiskCache(): DiskCache {
    return DiskCache.Builder()
        .directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "image_cache")
        .maxSizeBytes(124L * 1024 * 1024) // 124MB
        .build()
}

internal expect fun ComponentRegistry.Builder.addPlatformComponents()