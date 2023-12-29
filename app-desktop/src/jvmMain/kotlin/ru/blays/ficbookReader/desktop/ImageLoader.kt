package ru.blays.ficbookReader.desktop

import coil3.PlatformContext
import coil3.SingletonImageLoader
import ru.blays.ficbookReader.platformUtils.createImageLoader

fun initializeSingletonImageLoader() {
    if (isInitialized) return

    isInitialized = true

    SingletonImageLoader.set {
        createImageLoader(PlatformContext.INSTANCE)
    }
}

private var isInitialized = false