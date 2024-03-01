package ru.blays.ficbook.reader.shared.platformUtils

expect fun <T> runOnUiThread(block: () -> T): T