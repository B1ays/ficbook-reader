package ru.blays.ficbookReader.shared.ui.fanficPageComponents.declaration

import com.arkivanov.decompose.value.Value

interface FanficPageActionsComponent {
    val state: Value<State>

    fun sendIntent(intent: Intent)
    fun onOutput(output: Output)

    sealed class Intent {
        data class Follow(val follow: Boolean): Intent()
        data class Mark(val mark: Boolean): Intent()
    }

    sealed class Output {
        data object OpenComments: Output()
    }

    data class State(
        val follow: Boolean = false,
        val mark: Boolean = false
    )
}

interface InternalFanficPageActionsComponent: FanficPageActionsComponent {
    fun setValue(value: FanficPageActionsComponent.State)
}