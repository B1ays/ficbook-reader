package ru.blays.ficbookReader.shared.data.dto

import androidx.compose.runtime.Immutable

@Immutable
data class UserModelStable(
    val name: String = "",
    val href: String = "",
    val avatarUrl: String = ""
)
