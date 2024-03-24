package ru.blays.ficbook.reader.feature.fanficDownloader

import com.darkrockstudios.libraries.mpfilepicker.PlatformFile

expect suspend fun downloadFanficInEpub(
    file: PlatformFile,
    task: DownloadTask
): Boolean

data class DownloadTask(
    val format: String,
    val fanficID: String,
    val title: String,
)