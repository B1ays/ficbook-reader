package ru.blays.ficbook.utils

import androidx.compose.runtime.compositionLocalOf
import dev.chrisbanes.haze.HazeState

val LocalBlurState = compositionLocalOf { false }

val LocalHazeState = compositionLocalOf<HazeState> { throw IllegalStateException("HazeState not provided") }