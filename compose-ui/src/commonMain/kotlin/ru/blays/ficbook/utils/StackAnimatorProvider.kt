package ru.blays.ficbook.utils

import androidx.compose.animation.core.spring
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.androidPredictiveBackAnimatable
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.essenty.backhandler.BackHandler

@ExperimentalDecomposeApi
inline fun <reified C : Any, reified T : Any> defaultPredictiveAnimation(
    backHandler: BackHandler,
    noinline onBack: () -> Unit
) = predictiveBackAnimation<C, T>(
    backHandler = backHandler,
    fallbackAnimation = defaultStackAnimation(),
    selector = { backEvent, _, _ -> androidPredictiveBackAnimatable(backEvent) },
    onBack = onBack,
)

inline fun <reified C : Any, reified T : Any> defaultStackAnimation(): StackAnimation<C, T> = stackAnimation(
    fade(spring(stiffness = 300F, dampingRatio = .6F))
)