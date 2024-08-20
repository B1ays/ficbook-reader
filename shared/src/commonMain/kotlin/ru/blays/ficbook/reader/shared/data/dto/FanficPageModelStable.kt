package ru.blays.ficbook.reader.shared.data.dto

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class FanficPageModelStable(
    val fanficID: String,
    val name: String,
    val status: FanficStatusStable,
    val authors: List<FanficAuthorModelStable>,
    val fandoms: List<FandomModelStable>,
    val pairings: List<PairingModelStable>,
    val tags: List<FanficTagStable>,
    val coverUrl: String,
    val description: String,
    val dedication: String?,
    val authorComment: String?,
    val publicationRules: String,
    val subscribersCount: Int,
    val commentCount: Int,
    val pagesCount: Int,
    val liked: Boolean,
    val subscribed: Boolean,
    val inCollectionsCount: Int,
    val chapters: FanficChapterStable,
    val rewards: List<RewardModelStable>
)

@Serializable
@Immutable
sealed class FanficChapterStable {
    @Serializable
    data class SeparateChaptersModel(
        val chapters: List<Chapter>,
        val chaptersCount: Int
    ): FanficChapterStable() {
        @Serializable
        data class Chapter(
            val chapterID: String,
            val href: String,
            val name: String,
            val date: String,
            val commentsCount: Int,
            val lastWatchedCharIndex: Int = 0,
            val readed: Boolean = false
        )
    }

    @Serializable
    data class SingleChapterModel(
        val chapterID: String,
        val date: String,
        val text: String
    ): FanficChapterStable()

    val size: Int
        get() = when(this) {
            is SeparateChaptersModel -> chaptersCount
            is SingleChapterModel -> 1
        }
}

@Immutable
@Serializable
data class RewardModelStable(
    val message: String,
    val fromUser: String,
    val awardDate: String
)

@Serializable
@Immutable
data class FanficAuthorModelStable(
    val user: UserModelStable,
    val role: String
)
