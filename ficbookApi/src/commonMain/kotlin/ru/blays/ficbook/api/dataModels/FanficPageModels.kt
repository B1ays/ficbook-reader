package ru.blays.ficbook.api.dataModels

import kotlinx.serialization.Serializable

@Serializable
data class FanficPageModel(
    val id: String,
    val name: String,
    val status: FanficStatus,
    val authors: List<FanficAuthorModel>,
    val fandoms: List<FandomModel>,
    val pairings: List<PairingModel>,
    val tags: List<FanficTag>,
    val coverUrl: CoverUrl,
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
    val chapters: FanficChapter,
    val rewards: List<RewardModel>
)

@Serializable
sealed class FanficChapter {
    @Serializable
    data class SeparateChaptersModel(
        val chapters: List<Chapter>,
        val chaptersCount: Int
    ): FanficChapter() {
        @Serializable
        data class Chapter(
            val chapterID: String,
            val href: String,
            val name: String,
            val date: String,
            val commentsCount: Int
        )
    }

    @Serializable
    data class SingleChapterModel(
        val chapterID: String,
        val date: String,
        val text: String
    ): FanficChapter()

    val size: Int
        get() = when(this) {
            is SeparateChaptersModel -> chapters.size
            is SingleChapterModel -> 1
        }
}

@Serializable
data class RewardModel(
    val message: String,
    val fromUser: String,
    val awardDate: String
) {
    override fun toString(): String {
        return "$message от $fromUser"
    }
}

@Serializable
data class FanficAuthorModel(
    val user: UserModel,
    val role: String
)

