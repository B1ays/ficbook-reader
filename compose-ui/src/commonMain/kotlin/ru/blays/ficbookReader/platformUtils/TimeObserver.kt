package ru.blays.ficbookReader.platformUtils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

@Composable
expect fun rememberTimeObserver(timePattern: String = "HH:mm"): State<String>