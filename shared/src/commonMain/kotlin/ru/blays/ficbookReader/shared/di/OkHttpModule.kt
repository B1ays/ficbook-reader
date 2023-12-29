package ru.blays.ficbookReader.shared.di

import okhttp3.*
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
            .addNetworkInterceptor(
                interceptor = UserAgentInterceptor(
                    userAgent = USER_AGENT
                )
            )
            .cookieJar(get())
            .build()
    }
}

class UserAgentInterceptor(private val userAgent: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
            .newBuilder()
            .header("User-Agent", userAgent)
            .build()
        return chain.proceed(request)
    }
}

private const val USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3346.8 Safari/537.36"