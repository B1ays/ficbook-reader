package ru.blays.ficbookReader.platformUtils

import androidx.compose.runtime.Composable

@Composable
expect fun FullscreenContainer(enabled: Boolean, content: @Composable () -> Unit)