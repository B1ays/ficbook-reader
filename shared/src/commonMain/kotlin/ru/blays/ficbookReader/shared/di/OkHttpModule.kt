package ru.blays.ficbookReader.shared.di

import okhttp3.Cache
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.blays.ficbookReader.shared.data.cookieStorage.DynamicCookieJar
import ru.blays.ficbookReader.shared.platformUtils.getCacheDir

val okHttpModule = module {
    single { DynamicCookieJar() } bind CookieJar::class
    single {
        OkHttpClient.Builder()
            .cache(
                cache = Cache(
                    directory = getCacheDir(),
                    maxSize = 15 * 1024 * 1024
                )
            )
            .cookieJar(get())
            .build()
    }
}