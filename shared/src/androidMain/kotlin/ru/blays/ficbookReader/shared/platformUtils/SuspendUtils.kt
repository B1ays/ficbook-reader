package ru.blays.ficbookReader.shared.platformUtils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

actual fun <T> runOnUiThread(block: () -> T): T = runBlocking(Dispatchers.Main) {
    block()
}