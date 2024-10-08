package ru.blays.ficbook.reader.shared.data.dto

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class CommentModelStable(
    val commentID: String,
    val user: UserModelStable,
    val isOwnComment: Boolean,
    val isLiked: Boolean,
    val likedBy: List<FanficAuthorModelStable>,
    val date: String,
    val blocks: List<CommentBlockModelStable>,
    val likes: Int,
    val forFanfic: FanficShortcut?
)

@Serializable
@Immutable
data class CommentBlockModelStable(
    val quote: QuoteModelStable?,
    val text: String
)

@Serializable
@Immutable
data class QuoteModelStable(
    val quote: QuoteModelStable?,
    val userName: String,
    val text: String
)

@Serializable
@Immutable
data class FanficShortcut(
    val name: String,
    val href: String
)