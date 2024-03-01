package ru.blays.ficbook.utils

import androidx.compose.animation.core.spring
import androidx.compose.runtime.staticCompositionLocalOf
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimator
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.scale

val LocalStackAnimator = staticCompositionLocalOf<StackAnimator> {
    fade(spring()) + scale()
}