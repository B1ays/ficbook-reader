package ru.blays.ficbook.reader.shared.ui.readerComponents.declaration

import com.arkivanov.decompose.value.Value

interface VoteReaderComponent {
    val state: Value<State>

    fun sendIntent(intent: Intent)

    sealed class Intent {
        data class VoteForContinue(val vote: Boolean): Intent()
        data class Read(val read: Boolean): Intent()
    }

    data class State(
        val canVote: Boolean = false,
        val votedForContinue: Boolean = false,
        val readed: Boolean = false
    )
}