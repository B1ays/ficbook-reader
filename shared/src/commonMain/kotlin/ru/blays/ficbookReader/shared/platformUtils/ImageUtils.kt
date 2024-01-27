package ru.blays.ficbookReader.shared.platformUtils

import java.io.File

expect suspend fun downloadImageToFile(url: String, file: File, formatName: String): Boolean