package ru.blays.ficbookReader.utils

import androidx.compose.runtime.compositionLocalOf
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.StackAnimator

val LocalStackAnimator = compositionLocalOf<StackAnimator?> { null }