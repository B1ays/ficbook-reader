package ru.blays.ficbookReader.shared.platformUtils

expect fun <T> runOnUiThread(block: () -> T): T