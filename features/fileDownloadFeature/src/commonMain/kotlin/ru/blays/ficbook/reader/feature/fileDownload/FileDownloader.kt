package ru.blays.ficbook.reader.feature.fileDownload

import com.darkrockstudios.libraries.mpfilepicker.PlatformFile


public expect suspend fun downloadMPFile(url: String, file: PlatformFile)