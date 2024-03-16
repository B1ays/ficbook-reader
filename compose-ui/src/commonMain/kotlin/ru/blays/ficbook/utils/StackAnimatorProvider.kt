package ru.blays.ficbook.utils

import androidx.compose.animation.core.spring
import androidx.compose.runtime.staticCompositionLocalOf
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimator
import com.arkivanov.decompose.extensions.compose.stack.animation.fade

val LocalStackAnimator = staticCompositionLocalOf<StackAnimator> {
    fade(spring(stiffness = 300F, dampingRatio = .6F))
}