package ru.blays.ficbook.utils

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver

fun ColorScheme.surfaceColorAtAlpha(
    alpha: Float,
): Color = surfaceTint.copy(alpha = alpha).compositeOver(surface)
