package ru.blays.ficbookReader.shared.platformUtils

import androidx.annotation.ChecksSdkIntAtLeast

actual const val shareSupported = true

actual const val customTabsSupported = true

@ChecksSdkIntAtLeast
actual val dynamicColorSupported = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S