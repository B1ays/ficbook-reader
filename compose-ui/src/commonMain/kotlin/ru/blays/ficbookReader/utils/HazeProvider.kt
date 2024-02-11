package ru.blays.ficbookReader.utils

import androidx.compose.runtime.compositionLocalOf
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle

data class BlurConfig(
    val blurEnabled: Boolean,
    val style: HazeStyle
)

val LocalGlassEffectConfig = compositionLocalOf<BlurConfig> { throw IllegalStateException("lurConfig not provided") }

val LocalHazeState = compositionLocalOf<HazeState> { throw IllegalStateException("HazeState not provided") }