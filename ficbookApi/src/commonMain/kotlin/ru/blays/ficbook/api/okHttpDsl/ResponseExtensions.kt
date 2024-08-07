package ru.blays.ficbook.api.okHttpDsl

import okhttp3.ResponseBody
import okio.IOException

internal fun ResponseBody?.stringOrThrow(): String {
    return this?.string() ?: throw IOException("No response body")
}