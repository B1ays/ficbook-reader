package ru.blays.ficbookReader.shared.data.dto

import androidx.compose.runtime.Immutable

@Immutable
data class NotificationModelStable(
    val href: String,
    val title: String,
    val date: String,
    val text: String,
    val readed: Boolean,
    val type: NotificationType
)

@Immutable
data class NotificationCategoryStable(
    val type: NotificationType,
    val notificationsCount: Int
)

enum class NotificationType {
    ALL_NOTIFICATIONS,
    NEW_COMMENTS,
    SYSTEM_MESSAGES,
    NEW_WORKS_FOR_LIKED_REQUESTS,
    HELPDESK_RESPONSES,
    TEXT_CHANGES_IN_OWN_FANFIC,
    NEW_PRESENTS,
    NEW_ACHIEVEMENTS,
    TEXT_CHANGES_IN_EDITED_FANFIC,
    ERROR_MESSAGES,
    REQUESTS_FOR_COAUTHORSHIPS,
    REQUESTS_FOR_BETA_EDITING,
    REQUESTS_FOR_GAMMA_EDITING,
    NEW_WORKS_ON_MY_REQUESTS,
    DISCUSSION_IN_COMMENTS,
    DISCUSSION_IN_REQUEST_COMMENTS,
    PRIVATE_MESSAGES,
    UPDATES_FROM_SUBSCRIBED_AUTHORS,
    NEW_WORKS_IN_COLLECTIONS,
    UPDATES_IN_FANFICS,
    NEW_COMMENTS_FOR_REQUEST,
    NEW_FANFICS_REWARDS,
    NEW_COMMENTS_REWARDS,
    CHANGES_IN_HEADER_OF_WORK,
    COAUTHOR_ADD_NEW_CHAPTER,
    NEW_BLOGS,
    UNKNOWN;
}
