package ru.blays.ficbook.reader.shared.platformUtils

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

actual const val shareSupported = true

actual const val customTabsSupported = true

@ChecksSdkIntAtLeast
actual val dynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@ChecksSdkIntAtLeast
actual val blurSupported: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2