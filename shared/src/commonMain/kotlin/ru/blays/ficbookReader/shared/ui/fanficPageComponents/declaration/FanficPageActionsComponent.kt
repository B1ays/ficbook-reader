package ru.blays.ficbookReader.shared.ui.fanficPageComponents.declaration

import com.arkivanov.decompose.value.Value

interface FanficPageActionsComponent {
    val state: Value<State>

    fun onIntent(intent: Intent)

    sealed class Intent {
        data class Follow(val follow: Boolean): Intent()
        data class Mark(val mark: Boolean): Intent()
        data class Read(val read: Boolean): Intent()
    }

    data class State(
        val follow: Boolean = false,
        val mark: Boolean = false,
        val readed: Boolean = false
    )
}

interface InternalFanficPageActionsComponent: FanficPageActionsComponent {
    fun setValue(value: FanficPageActionsComponent.State)
}