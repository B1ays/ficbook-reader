package ru.blays.ficbook.reader.shared.platformUtils

import android.content.Context
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import ru.blays.ficbook.reader.shared.data.chromePackages
import ru.blays.ficbook.reader.shared.utils.isAnyPackageInstalled

actual const val shareSupported = true

actual val KoinComponent.customTabsSupported
    get() = get<Context>().isAnyPackageInstalled(chromePackages)

@ChecksSdkIntAtLeast
actual val dynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@ChecksSdkIntAtLeast
actual val blurSupported: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2