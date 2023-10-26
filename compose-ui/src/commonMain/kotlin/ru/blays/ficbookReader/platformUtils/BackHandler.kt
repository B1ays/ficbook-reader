package ru.blays.ficbookReader.platformUtils

import androidx.compose.runtime.Composable

@Composable
expect fun BackHandler(enable: Boolean, onBack: () -> Unit)