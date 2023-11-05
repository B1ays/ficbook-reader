package ru.blays.ficbookReader.shared.platformUtils

import java.awt.Desktop
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException


actual fun openUrl(url: String) {
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