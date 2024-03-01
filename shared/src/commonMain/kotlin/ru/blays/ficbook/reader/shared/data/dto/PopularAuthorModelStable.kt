package ru.blays.ficbook.reader.shared.data.dto

import androidx.compose.runtime.Immutable

@Immutable
data class PopularAuthorModelStable(
    val user: UserModelStable,
    val position: Int,
    val subscribersInfo: String
)