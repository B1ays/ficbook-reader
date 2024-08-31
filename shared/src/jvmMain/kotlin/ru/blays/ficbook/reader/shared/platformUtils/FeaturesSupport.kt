package ru.blays.ficbook.reader.shared.platformUtils

import org.koin.core.component.KoinComponent

actual const val shareSupported = false

actual val KoinComponent.customTabsSupported
    get()= false

actual const val dynamicColorSupported = false

actual const val blurSupported: Boolean = true