package ru.blays.ficbookReader.platformUtils

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import dev.chrisbanes.haze.haze


actual fun Modifier.blurPlatform(
    areas: Array<Rect>,
    backgroundColor: Color,
    tint: Color,
    blurRadius: Dp,
    noiseFactor: Float,
    edgeTreatment: BlurredEdgeTreatment
): Modifier = this then haze(
    area = areas,
    backgroundColor = backgroundColor,
    tint = tint,
    blurRadius = blurRadius,
    noiseFactor = noiseFactor
)

actual const val blurSupported: Boolean = true