package ru.blays.ficbook.api.dataModels

import ru.blays.ficbook.api.data.Section


data class AuthorProfileModel(
    val authorMain: AuthorMainInfo,
    val authorInfo: AuthorInfoModel,
    val availableTabs: List<AuthorProfileTabs>
) {
    override fun toString(): String =
        """
            AuthorProfileModel(
                authorMain: $authorMain,
                authorInfo: $authorInfo
            )
        """.trimIndent()
}

data class AuthorMainInfo(
    val name: String,
    val id: String,
    val avatarUrl: String,
    val profileCoverUrl: String,
    val subscribers: Int,
    val subscribed: Boolean,
    internal val availableTabs: List<AuthorProfileTabs>
)

data class AuthorInfoModel(
    val about: String,
    val contacts: String,
    val support: String
) {
    override fun toString(): String = """
        AuthorInfoModel(
            about: $about,
            contacts: $contacts,
            support: $support
        )
    """.trimIndent()
}

data class BlogPostCardModel(
    val id: String,
    val title: String,
    val date: String,
    val text: String,
    val likes: Int
) {
    override fun toString(): String = """
        BlogPostCardModel(
            href: $id,
            title: $title,
            date: $date,
            text: $text,
            likes: $likes
        )
    """.trimIndent()
}

data class BlogPostPageModel(
    val title: String,
    val date: String,
    val text: String,
    val likes: Int
)

data class AuthorPresentModel(
    val pictureUrl: String,
    val text: String,
    val user: UserModel
) {
    override fun toString(): String = """
        AuthorPresentModel(
            pictureUrl: $pictureUrl,
            text: $text,
            user: $user
        )
    """.trimIndent()
}

data class AuthorFanficPresentModel(
    val pictureUrl: String,
    val text: String,
    val user: UserModel,
    val forWork: Section
) {
    override fun toString(): String = """
        AuthorPresentModel(
            pictureUrl: $pictureUrl,
            text: $text,
            user: $user,
            forWork: $forWork
        )
    """.trimIndent()
}

data class AuthorCommentPresentModel(
    val pictureUrl: String,
    val text: String,
    val user: UserModel,
    val forWork: Section
) {
    override fun toString(): String = """
        AuthorPresentModel(
            pictureUrl: $pictureUrl,
            text: $text,
            user: $user,
            forWork: $forWork
        )
    """.trimIndent()
}

enum class AuthorProfileTabs(internal val href: String) {
    BLOG("blog"),
    WORKS("profile/works"),
    WORKS_COAUTHOR("profile/coauthor"),
    WORKS_BETA("profile/beta"),
    WORKS_GAMMA("profile/gamma"),
    REQUESTS("requests"),
    COLLECTIONS("collections"),
    PRESENTS("presents"),
    COMMENTS("comments");

    companion object {
        fun findForPath(path: String): AuthorProfileTabs? {
            return entries.find { path.contains(it.href) }
        }
    }
}