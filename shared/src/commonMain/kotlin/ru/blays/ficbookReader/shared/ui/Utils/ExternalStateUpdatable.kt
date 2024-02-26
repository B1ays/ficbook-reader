package ru.blays.ficbookReader.shared.ui.Utils

interface ExternalStateUpdatable <T> {
    fun updateState(block: (T) -> T)
}