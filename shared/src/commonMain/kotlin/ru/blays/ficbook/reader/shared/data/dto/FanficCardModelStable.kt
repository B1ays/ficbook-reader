package ru.blays.ficbook.reader.shared.data.dto

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class FanficCardModelStable(
    val href: String,
    val id: String,
    val title: String,
    val status: FanficStatusStable,
    val author: UserModelStable,
    val originalAuthor: UserModelStable?,
    val fandoms: List<FandomModelStable>,
    val pairings: List<PairingModelStable>,
    val updateDate: String,
    val size: String,
    val readInfo: ReadBadgeModelStable?,
    val tags: List<FanficTagStable>,
    val description: String,
    val coverUrl: String
)

@Serializable
@Immutable
data class FanficStatusStable(
    val direction: FanficDirection,
    val rating: FanficRating,
    val status: FanficCompletionStatus,
    val hot: Boolean,
    val likes: Int,
    val trophies: Int
)


@Serializable
@Immutable
data class FanficTagStable(
    val name: String,
    val isAdult: Boolean,
    val href: String
)

@Serializable
@Immutable
data class ReadBadgeModelStable(
    val readDate: String,
    val hasUpdate: Boolean
)