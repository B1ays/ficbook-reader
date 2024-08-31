package ru.blays.ficbook.reader.shared.di

import okhttp3.*
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.blays.ficbook.reader.shared.data.cookieStorage.DynamicCookieJar
import ru.blays.ficbook.reader.shared.platformUtils.getCacheDir
import ru.blays.ficbook.reader.shared.proxy.IProxyHolder
import ru.blays.ficbook.reader.shared.proxy.ProxyHolder

val okHttpModule = module {
    singleOf(::DynamicCookieJar) bind CookieJar::class
    singleOf(::ProxyHolder) bind IProxyHolder::class
    single {
        val proxyHolder: ProxyHolder = get()
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
            .proxySelector(proxyHolder)
            .proxyAuthenticator(proxyHolder.authenticator)
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

private const val USER_AGENT = "AppleWebKit/605.1"
/*"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3346.8 Safari/537.36"*/