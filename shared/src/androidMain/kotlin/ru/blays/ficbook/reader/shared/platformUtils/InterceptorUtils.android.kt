package ru.blays.ficbook.reader.shared.platformUtils

import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.Interceptor
import org.koin.core.scope.Scope

actual fun Scope.getPlatformInterceptors(): List<Interceptor> = listOf(
    ChuckerInterceptor(get())
)