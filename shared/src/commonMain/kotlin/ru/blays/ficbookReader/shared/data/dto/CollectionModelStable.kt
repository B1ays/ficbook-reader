package ru.blays.ficbookReader.shared.data.dto

import androidx.compose.runtime.Immutable

@Immutable
data class CollectionModelStable(
    val href: String,
    val name: String,
    val size: Int,
    val private: Boolean,
    val owner: UserModelStable
)
