package ru.blays.ficbookReader.shared.data.dto

import androidx.compose.runtime.Immutable

@Immutable
data class PairingModelStable(
    val character: String,
    val href: String,
    val isHighlighted: Boolean
)
