package ru.blays.ficbookReader.shared.data.dto

import androidx.compose.runtime.Immutable

@Immutable
data class CommentModelStable(
    val commentID: String,
    val user: UserModelStable,
    val isOwnComment: Boolean,
    val date: String,
    val blocks: List<CommentBlockModelStable>,
    val likes: Int,
    val forFanfic: FanficShortcut?
)

@Immutable
data class CommentBlockModelStable(
    val quote: QuoteModelStable?,
    val text: String
)

@Immutable
data class QuoteModelStable(
    val quote: QuoteModelStable?,
    val userName: String,
    val text: String
)

@Immutable
data class FanficShortcut(
    val name: String,
    val href: String
)