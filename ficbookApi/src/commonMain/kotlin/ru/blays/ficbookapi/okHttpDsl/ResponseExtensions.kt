package ru.blays.ficbookapi.okHttpDsl

import okhttp3.ResponseBody
import okio.IOException

fun ResponseBody?.stringOrThrow(): String {
    return this?.string() ?: throw IOException("No response body")
}