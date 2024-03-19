package ru.blays.ficbook.reader.shared.platformUtils

import android.content.Context
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbook.reader.shared.data.chromePackages
import ru.blays.ficbook.reader.shared.utils.isAnyPackageInstalled

actual const val shareSupported = true

actual val customTabsSupported by lazy { getKoin().get<Context>().isAnyPackageInstalled(chromePackages) }

@ChecksSdkIntAtLeast
actual val dynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@ChecksSdkIntAtLeast
actual val blurSupported: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2