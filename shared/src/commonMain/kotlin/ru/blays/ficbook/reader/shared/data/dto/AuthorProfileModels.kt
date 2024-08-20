package ru.blays.ficbook.reader.shared.data.dto

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import ru.blays.ficbook.api.dataModels.AuthorProfileTabs

@Serializable
@Immutable
data class AuthorProfileModelStable(
    val authorMain: AuthorMainInfoStable,
    val authorInfo: AuthorInfoModelStable,
    internal val availableTabs: List<AuthorProfileTabs>
)

@Serializable
@Immutable
data class AuthorMainInfoStable(
    val name: String,
    val realID: String,
    val relativeID: String,
    val avatarUrl: String,
    val profileCoverUrl: String,
    val subscribers: Int,
    val subscribed: Boolean
)

@Serializable
@Immutable
data class AuthorInfoModelStable(
    val about: String,
    val contacts: String,
    val support: String
)

@Serializable
@Immutable
data class BlogPostCardModelStable(
    val id: String,
    val title: String,
    val date: String,
    val text: String,
    val likes: Int
)

@Serializable
@Immutable
data class BlogPostModelStable(
    val title: String,
    val date: String,
    val text: String,
    val likes: Int
)

@Serializable
@Immutable
data class AuthorPresentModelStable(
    val pictureUrl: String,
    val text: String,
    val user: UserModelStable
)

@Serializable
@Immutable
data class AuthorFanficPresentModelStable(
    val pictureUrl: String,
    val text: String,
    val user: UserModelStable,
    val forWork: Section
)

@Serializable
@Immutable
data class AuthorCommentPresentModelStable(
    val pictureUrl: String,
    val text: String,
    val user: UserModelStable,
    val forWork: Section
)
