package ru.blays.ficbook.reader.shared.platformUtils

import org.koin.core.component.KoinComponent

expect val shareSupported: Boolean

expect val KoinComponent.customTabsSupported: Boolean

expect val dynamicColorSupported: Boolean

expect val blurSupported: Boolean