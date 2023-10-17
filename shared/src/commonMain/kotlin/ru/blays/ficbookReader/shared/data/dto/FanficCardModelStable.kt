package ru.blays.ficbookReader.shared.data.dto

import androidx.compose.runtime.Immutable

@Immutable
data class FanficCardModelStable(
    val href: String,
    val title: String,
    val status: FanficStatusStable,
    val author: String,
    val fandom: String,
    val updateDate: String,
    val readInfo: ReadBadgeModelStable?,
    val tags: List<FanficTagStable>,
    val description: String,
    val coverUrl: String
)

@Immutable
data class FanficStatusStable(
    val direction: FanficDirection,
    val rating: FanficRating,
    val status: FanficCompletionStatus,
    val hot: Boolean,
    val likes: Int,
    val trophies: Int
)

@Immutable
data class FanficTagStable(
    val name: String,
    val isAdult: Boolean,
    val href: String
)

@Immutable
data class ReadBadgeModelStable(
    val readDate: String,
    val hasUpdate: Boolean
)