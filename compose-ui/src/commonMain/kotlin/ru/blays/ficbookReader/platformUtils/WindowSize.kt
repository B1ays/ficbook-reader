package ru.blays.ficbookReader.platformUtils

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntSize

@Composable
expect fun WindowSize(): IntSize

expect val landscapeModeWidth: Int