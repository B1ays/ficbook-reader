package ru.blays.ficbookReader.shared.data.dto

import androidx.compose.runtime.Immutable

@Immutable
data class FandomModelStable(
    val href: String = "",
    val name: String = "",
    val description: String = ""
)
