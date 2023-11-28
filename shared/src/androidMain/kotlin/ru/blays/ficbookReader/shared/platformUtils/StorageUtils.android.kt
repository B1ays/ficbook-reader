package ru.blays.ficbookReader.shared.platformUtils

import android.content.Context
import org.koin.mp.KoinPlatform.getKoin
import java.io.File

actual fun getCacheDir(): File {
    val context: Context by getKoin().inject()
    return context.cacheDir
}