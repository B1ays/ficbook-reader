package ru.blays.ficbookReader.shared.ui.fanficListComponents.declaration

import com.arkivanov.decompose.value.Value

interface FanficQuickActionsComponent {
    val state: Value<State>

    fun sendIntent(intent: Intent)

    sealed class Intent {
        data object Initialize: Intent()
        data object Like: Intent()
        data object Subscribe: Intent()
        data object Read: Intent()
    }

    data class State(
        val liked: Boolean,
        val subscribed: Boolean,
        val readed: Boolean,
        val loading: Boolean,
        val error: Boolean,
        val errorMessage: String?
    )
}