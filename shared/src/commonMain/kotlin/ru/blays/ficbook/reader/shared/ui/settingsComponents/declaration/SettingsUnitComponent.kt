package ru.blays.ficbook.reader.shared.ui.settingsComponents.declaration

import kotlinx.coroutines.flow.StateFlow

interface SettingsUnitComponent <T: Any> {
    val state: StateFlow<T>

    fun onIntent(intent: Intent<T>)

    sealed class Intent<T> {
        data class ChangeValue<T>(val value: T): Intent<T>()
    }
}