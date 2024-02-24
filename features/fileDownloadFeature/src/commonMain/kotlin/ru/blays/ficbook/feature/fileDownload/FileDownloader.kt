package ru.blays.ficbook.feature.fileDownload

import com.darkrockstudios.libraries.mpfilepicker.PlatformFile


public expect suspend fun downloadMPFile(url: String, file: PlatformFile)