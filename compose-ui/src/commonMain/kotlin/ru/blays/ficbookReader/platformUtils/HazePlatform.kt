package ru.blays.ficbookReader.platformUtils

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

expect fun Modifier.blurPlatform(
    areas: Array<Rect>,
    backgroundColor: Color,
    tint: Color,
    blurRadius: Dp/* = HazeDefaults.blurRadius*/,
    noiseFactor: Float/* = HazeDefaults.noiseFactor*/,
    edgeTreatment: BlurredEdgeTreatment = BlurredEdgeTreatment.Rectangle
): Modifier