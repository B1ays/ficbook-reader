package ru.blays.ficbook.reader.shared.data.dto

import androidx.compose.runtime.Immutable
import ru.blays.ficbook.api.dataModels.AuthorProfileTabs

@Immutable
data class AuthorProfileModelStable(
    val authorMain: AuthorMainInfoStable,
    val authorInfo: AuthorInfoModelStable,
    internal val availableTabs: List<AuthorProfileTabs>
)

@Immutable
data class AuthorMainInfoStable(
    val name: String,
    val id: String,
    val avatarUrl: String,
    val profileCoverUrl: String,
    val subscribers: Int,
    val subscribed: Boolean
)

@Immutable
data class AuthorInfoModelStable(
    val about: String,
    val contacts: String,
    val support: String
)

@Immutable
data class BlogPostCardModelStable(
    val id: String,
    val title: String,
    val date: String,
    val text: String,
    val likes: Int
)

data class BlogPostModelStable(
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
