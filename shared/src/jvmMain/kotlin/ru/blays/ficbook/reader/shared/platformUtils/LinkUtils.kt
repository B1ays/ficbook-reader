package ru.blays.ficbook.reader.shared.platformUtils

import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException


actual fun openInBrowser(url: String) {
    if (Desktop.isDesktopSupported()) {
        val desktop = Desktop.getDesktop()
        try {
            desktop.browse(URI(url))
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    } else {
        val runtime = Runtime.getRuntime()
        try {
            runtime.exec(arrayOf("xdg-open", url))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

actual fun copyToClipboard(text: String) {
    val stringSelection = StringSelection(text)
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    clipboard.setContents(stringSelection, null)
}

actual fun shareText(text: String) = Unit