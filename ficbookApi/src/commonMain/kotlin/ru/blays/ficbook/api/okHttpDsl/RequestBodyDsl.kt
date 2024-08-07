package ru.blays.ficbook.api.okHttpDsl

import okhttp3.FormBody
import okhttp3.MultipartBody
import okhttp3.RequestBody

internal fun formBody(
    block: FormBody.Builder.() -> Unit
): FormBody {
    val builder = FormBody.Builder().apply(block)
    return builder.build()
}

internal fun multipartBody(
    block: MultipartBody.Builder.() -> Unit
): RequestBody {
    val builder = MultipartBody.Builder().apply(block)
    return builder.build()
}

internal fun bodyPart(name: String, value: String): MultipartBody.Part {
    return MultipartBody.Part.createFormData(
        name = name,
        value = value
    )
}