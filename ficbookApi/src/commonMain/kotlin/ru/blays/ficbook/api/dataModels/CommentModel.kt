package ru.blays.ficbook.api.dataModels

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class CommentsListResult(
    val comments: List<CommentModel>,
    val hasNextPage: Boolean
)

data class CommentModel(
    val commentID: String,
    val user: UserModel,
    val isOwnComment: Boolean,
    val isLiked: Boolean,
    val date: String,
    val blocks: List<CommentBlockModel>,
    val likes: Int,
    val likedBy: List<FanficAuthorModel>,
    val forFanfic: FanficShortcut?
) {
    override fun toString(): String {
        return """
            user: $user
            date: $date
            blocks: ${blocks.joinToString("\n")}
            likes: $likes
        """.trimIndent()
    }
}

data class CommentBlockModel(
    val quote: QuoteModel?,
    val text: String
) {
    override fun toString(): String {
        return """
            quote: $quote
            text: $text
        """.trimIndent()
    }
}

data class QuoteModel(
    val quote: QuoteModel?,
    val userName: String,
    val text: String
) {
    override fun toString(): String {
        return """
            quote: $quote
            userName: $userName
            text: $text
        """.trimIndent()
    }
}

data class FanficShortcut(
    val name: String,
    val href: String
)

@Serializable
data class CommentMetadata(
    @SerialName("avatar_decoration_style")
    val avatarDecorationStyle: String,
    @SerialName("comment")
    val comment: String,
    @SerialName("date_create")
    val dateCreate: String,
    @SerialName("fanfic_id")
    val fanficId: Int,
    @SerialName("fanfic_slug")
    val fanficSlug: String,
    @SerialName("free_rewards")
    val freeRewards: Int,
    @SerialName("id")
    val id: Int,
    @SerialName("likeBadges")
    val likeBadges: List<LikeBadge> = emptyList(),
    @SerialName("like_cnt")
    val likeCnt: Int,
    @SerialName("liked")
    val liked: Boolean,
    @SerialName("name")
    val name: String?,
    @SerialName("part_id")
    val partId: Int,
    @SerialName("user_avatar")
    val userAvatar: String,
    @SerialName("user_id")
    val userId: Int,
    @SerialName("user_is_premium")
    val userIsPremium: Boolean,
    @SerialName("user_slug")
    val userSlug: String,
    @SerialName("user_status")
    val userStatus: Int,
    @SerialName("user_username")
    val userUsername: String
) {
    @Serializable
    data class LikeBadge(
        @SerialName("avatarUrl")
        val avatarUrl: String,
        @SerialName("id")
        val id: Int,
        @SerialName("type")
        val type: String,
        @SerialName("username")
        val username: String
    )
}