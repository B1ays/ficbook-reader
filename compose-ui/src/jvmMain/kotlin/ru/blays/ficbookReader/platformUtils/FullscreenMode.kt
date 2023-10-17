package ru.blays.ficbookReader.platformUtils

import androidx.compose.runtime.Composable

@Composable
actual fun FullscreenContainer(enabled: Boolean, content: @Composable () -> Unit) = content()