package ru.blays.ficbook.reader.shared.components.fanficPageComponents.declaration

import com.arkivanov.decompose.value.Value
import com.darkrockstudios.libraries.mpfilepicker.PlatformFile

interface DownloadFanficComponent {
    val formats: Array<FileFormat>

    val state: Value<State>

    fun sendIntent(intent: Intent)

    sealed class Intent {
        data object Close: Intent()
        data object CloseFilePicker: Intent()
        data class PickFile(val format: FileFormat): Intent()
        data class Download(val file: PlatformFile): Intent()
    }

    data class State(
        val showFilePicker: Boolean,
        val fanficName: String,
        val selectedFormat: String?
    )

    data class FileFormat(
        val extension: String,
        val description: String
    )
}