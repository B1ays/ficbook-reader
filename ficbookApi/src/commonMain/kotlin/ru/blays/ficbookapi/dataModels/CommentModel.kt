package ru.blays.ficbookapi.dataModels

data class CommentsListResult(
    val comments: List<CommentModel>,
    val hasNextPage: Boolean
)

data class CommentModel(
    val commentID: String,
    val user: UserModel,
    val isOwnComment: Boolean,
    val date: String,
    val blocks: List<CommentBlockModel>,
    val likes: Int,
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