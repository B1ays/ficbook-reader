package ru.blays.ficbook.reader.shared.ui.Utils

interface ExternalStateUpdatable <T> {
    fun updateState(block: (T) -> T)
}