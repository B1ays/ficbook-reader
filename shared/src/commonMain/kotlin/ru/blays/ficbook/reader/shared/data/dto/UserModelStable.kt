package ru.blays.ficbook.reader.shared.data.dto

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class UserModelStable(
    val name: String = "",
    val href: String = "",
    val avatarUrl: String = ""
)
