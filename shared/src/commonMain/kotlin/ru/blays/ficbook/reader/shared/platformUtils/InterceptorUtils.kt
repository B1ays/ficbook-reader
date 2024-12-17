package ru.blays.ficbook.reader.shared.platformUtils

import okhttp3.Interceptor
import org.koin.core.scope.Scope

expect fun Scope.getPlatformInterceptors(): List<Interceptor>