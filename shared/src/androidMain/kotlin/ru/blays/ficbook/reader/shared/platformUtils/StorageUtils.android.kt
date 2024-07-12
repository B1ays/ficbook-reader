package ru.blays.ficbook.reader.shared.platformUtils

import android.content.Context
import org.koin.mp.KoinPlatform.getKoin
import java.io.File

actual fun getCacheDir(): File {
    val context: Context = getKoin().get()
    return context.cacheDir
}

actual fun getFilesDir(): File {
    val context: Context = getKoin().get()
    return context.filesDir
}