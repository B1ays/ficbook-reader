package ru.blays.ficbook.reader.shared.components.Utils

interface ExternalStateUpdatable <T> {
    fun updateState(block: (T) -> T)
}