package ru.blays.ficbookReader.shared.platformUtils

import ru.blays.ficbookReader.shared.filesDirPath
import java.io.File

actual fun getCacheDir(): File {
    return File(filesDirPath)
}