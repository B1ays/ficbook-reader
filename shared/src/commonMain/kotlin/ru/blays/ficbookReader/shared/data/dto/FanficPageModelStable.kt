package ru.blays.ficbookReader.shared.data.dto

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
data class FanficPageModelStable(
    val fanficID: String,
    val name: String,
    val status: FanficStatusStable,
    val author: UserModelStable,
    var fandoms: List<FandomModelStable>,
    val coverUrl: String,
    val tags: List<FanficTagStable>,
    val description: String,
    val subscribersCount: Int,
    val commentCount: Int,
    val pagesCount: Int,
    val liked: Boolean,
    val subscribed: Boolean,
    val inCollectionsCount: Int,
    val chapters: List<FanficChapterStable>,
    val rewards: List<RewardModelStable>
)

@Serializable
@Immutable
sealed class FanficChapterStable {
    abstract val lastWatchedCharIndex: Int
    abstract val readed: Boolean

    @Serializable
    @Immutable
    data class SeparateChapterModel(
        val href: String,
        val name: String,
        val date: String,
        val commentsCount: Int,
        val commentsHref: String,
        override val lastWatchedCharIndex: Int = 0,
        override val readed: Boolean = false
    ): FanficChapterStable()

    @Serializable
    @Immutable
    data class SingleChapterModel(
        val date: String,
        val commentsCount: Int,
        val commentsHref: String,
        val text: String,
        override val lastWatchedCharIndex: Int = 0,
        override val readed: Boolean = false
    ): FanficChapterStable()
}

@Immutable
@Serializable
data class RewardModelStable(
    val message: String,
    val fromUser: String,
    val awardDate: String
)
