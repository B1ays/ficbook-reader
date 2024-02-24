package ru.blays.ficbook.feature.fileDownload

import com.darkrockstudios.libraries.mpfilepicker.PlatformFile
import kotlinx.coroutines.coroutineScope
import java.nio.file.Files
import java.nio.file.StandardCopyOption

actual suspend fun downloadMPFile(url: String, file: PlatformFile) = coroutineScope {
    val responseResult = makeDownloadResponse(url)
    if(responseResult == null) {
        showNotification(
            title = "Ошибка загрузки файла",
            message = "Сервер недоступен",
            iconType = PopupIconType.ERROR
        )
        return@coroutineScope
    }

    val inputStream = responseResult.inputStream

    showNotification(
        title = "Начата загрузка файла",
        message = file.file.name,
        iconType = PopupIconType.INFO
    )

    try {
        Files.copy(
            inputStream,
            file.file.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )
    } catch (e: Exception) {
        e.printStackTrace()
        showNotification(
            title = "Ошибка загрузки файла",
            message = e.message ?: "Unknown error",
            iconType = PopupIconType.ERROR
        )
    } finally {
        inputStream.close()
        showNotification(
            title = "Завершена загрузка файла",
            message = file.file.name,
            iconType = PopupIconType.INFO
        )
    }
}