package ru.blays.ficbook.reader.shared.platformUtils

import ru.blays.ficbook.reader.shared.cacheDirPath
import ru.blays.ficbook.reader.shared.filesDirPath
import java.io.File

actual fun getCacheDir(): File {
    return File(cacheDirPath)
}

actual fun getFilesDir(): File {
    return File(filesDirPath)
}