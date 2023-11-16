package ru.blays.ficbookReader.shared.platformUtils

expect fun openInBrowser(url: String)
expect fun copyToClipboard(text: String)
expect fun shareText(text: String)