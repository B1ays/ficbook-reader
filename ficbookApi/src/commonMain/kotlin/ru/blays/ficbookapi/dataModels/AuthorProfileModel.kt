package ru.blays.ficbookapi.dataModels

import ru.blays.ficbookapi.data.Section
import ru.blays.ficbookapi.data.SectionWithQuery

data class AuthorProfileModel(
    val authorMain: AuthorMainInfo,
    val authorInfo: AuthorInfoModel,
    val authorBlogHref: String,
    val authorWorks: SectionWithQuery?,
    val authorWorksAsCoauthor: SectionWithQuery?,
    val authorWorksAsBeta: SectionWithQuery?,
    val authorWorksAsGamma: SectionWithQuery?,
    val authorPresentsHref: String,
    val authorCommentsHref: String
) {
    override fun toString(): String =
        """
            AuthorProfileModel(
                authorMain: $authorMain,
                authorInfo: $authorInfo,
                authorBlog: $authorBlogHref,
                authorWorks: $authorWorks,
                authorWorksAsCoauthor: $authorWorksAsCoauthor,
                authorWorksAsBeta: $authorWorksAsBeta,
                authorWorksAsGamma: $authorWorksAsGamma,
                authorPresentHref: $authorPresentsHref,
                authorCommentsHref: $authorCommentsHref
            )
        """.trimIndent()
}

data class AuthorMainInfo(
    val name: String,
    val id: String,
    val avatarUrl: String,
    val profileCoverUrl: String,
    val subscribers: Int
) {
    internal var availableTabs: List<AuthorProfileTabs> = emptyList()
    
    override fun toString(): String = """
        AuthorMainInfo(
            name: $name,
            id: $id,
            avatarUrl: $avatarUrl,
            profileCoverUrl: $profileCoverUrl,
            subscribers count: $subscribers
        )
    """.trimIndent()
}

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

data class AuthorCommentsModel(
    val commentsForFanfics: List<CommentModel>, // TODO realize comment model
    val commentsForRequests: List<CommentModel>
)

data class BlogPostCardModel(
    val href: String,
    val title: String,
    val date: String,
    val text: String,
    val likes: Int
) {
    override fun toString(): String = """
        BlogPostCardModel(
            href: $href,
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

internal enum class AuthorProfileTabs(val href: String) {
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