package ru.blays.ficbook.reader.feature.fileDownload

import org.lwjgl.util.tinyfd.TinyFileDialogs

internal fun showNotification(
    title: String,
    message: String,
    iconType: PopupIconType
) {
    TinyFileDialogs.tinyfd_messageBox(
        title,
        message,
        "ok",
        iconType.value,
        false
    )
}

internal enum class PopupIconType(val value: String) {
    INFO("info"),
    WARNING("warning"),
    ERROR("error")
}