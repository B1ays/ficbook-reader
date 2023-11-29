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

@Immutable
data class CollectionSortParamsStable(
    val availableSortParams: List<Pair<String, String>>,
    val availableDirections: List<Pair<String, String>>,
    val availableFandoms: List<Pair<String, String>>
)