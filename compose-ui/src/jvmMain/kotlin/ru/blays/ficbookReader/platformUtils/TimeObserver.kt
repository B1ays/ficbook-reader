package ru.blays.ficbookReader.platformUtils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

@Composable
actual fun rememberTimeObserver(
    timePattern: String
): State<String> {
    return mutableStateOf("")
}