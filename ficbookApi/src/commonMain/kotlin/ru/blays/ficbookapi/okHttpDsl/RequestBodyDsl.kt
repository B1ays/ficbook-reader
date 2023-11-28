package ru.blays.ficbookapi.okHttpDsl

import okhttp3.FormBody

fun formBody(
    block: FormBody.Builder.() -> Unit
): FormBody {
    val builder = FormBody.Builder().apply(block)
    return builder.build()
}