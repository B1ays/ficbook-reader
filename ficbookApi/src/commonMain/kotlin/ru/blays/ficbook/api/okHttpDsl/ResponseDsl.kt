package ru.blays.ficbook.api.okHttpDsl

import okhttp3.*

suspend fun OkHttpClient.get(url: HttpUrl): Response {
    return newCall(
        request = Request.Builder()
            .url(url)
            .build()
    ).execute()
}

suspend fun OkHttpClient.get(
    block: Request.Builder.() -> Unit
): Response {
    val builder = Request.Builder().apply(block)
    return newCall(
        request = builder.build()
    ).execute()
}

suspend fun OkHttpClient.get(
    url: String,
    block: Request.Builder.() -> Unit
) {
    val builder = Request.Builder()
        .url(url)
        .apply(block)
    newCall(
        request = builder.build()
    ).execute()
}

suspend fun OkHttpClient.post(
    url: HttpUrl,
    body: RequestBody
): Response {
    return newCall(
        request = Request.Builder()
            .url(url)
            .post(body)
            .build()
    ).execute()
}

suspend fun OkHttpClient.post(
    body: RequestBody,
    block: Request.Builder.() -> Unit
): Response {
    return newCall(
        request = Request.Builder()
            .post(body)
            .apply(block)
            .build()
    ).execute()
}