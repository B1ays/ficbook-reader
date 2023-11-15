package ru.blays.ficbookReader.shared.data.dto

import androidx.compose.runtime.Immutable

@Immutable
data class AuthorProfileModelStable(
    val authorMain: AuthorMainInfoStable,
    val authorInfo: AuthorInfoModelStable,
    val authorBlogHref: String,
    val authorWorks: SectionWithQuery?,
    val authorWorksAsCoauthor: SectionWithQuery?,
    val authorWorksAsBeta: SectionWithQuery?,
    val authorWorksAsGamma: SectionWithQuery?,
    val authorPresentsHref: String,
    val authorCommentsHref: String
)

@Immutable
data class AuthorMainInfoStable(
    val name: String,
    val id: String,
    val avatarUrl: String,
    val profileCoverUrl: String,
    val subscribers: Int
)

@Immutable
data class AuthorInfoModelStable(
    val about: String,
    val contacts: String,
    val support: String
)

@Immutable
data class BlogPostCardModelStable(
    val href: String,
    val title: String,
    val date: String,
    val text: String,
    val likes: Int
)

data class BlogPostPageModelStable(
    val title: String,
    val date: String,
    val text: String,
    val likes: Int
)
@Immutable
data class AuthorPresentModelStable(
    val pictureUrl: String,
    val text: String,
    val user: UserModelStable
)

@Immutable
data class AuthorFanficPresentModelStable(
    val pictureUrl: String,
    val text: String,
    val user: UserModelStable,
    val forWork: Section
)

@Immutable
data class AuthorCommentPresentModelStable(
    val pictureUrl: String,
    val text: String,
    val user: UserModelStable,
    val forWork: Section
)
