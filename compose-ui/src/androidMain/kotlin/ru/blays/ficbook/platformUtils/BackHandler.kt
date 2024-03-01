package ru.blays.ficbook.platformUtils

import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler as BackHandlerActivity

@Composable
actual fun BackHandler(enable: Boolean, onBack: () -> Unit) = BackHandlerActivity(enable, onBack)