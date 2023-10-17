package ru.blays.ficbookReader.shared.data.dto

import androidx.compose.runtime.Immutable

@Immutable
data class UserModelStable(
    val name: String = "",
    val userID: String = "",
    val avatarUrl: String = ""
)
