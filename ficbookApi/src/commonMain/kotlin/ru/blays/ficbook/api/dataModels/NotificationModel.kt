package ru.blays.ficbook.api.dataModels

data class NotificationModel(
    val href: String,
    val title: String,
    val date: String,
    val text: String,
    val readed: Boolean,
    val type: NotificationType
)

data class NotificationCategory(
    val type: NotificationType,
    val notificationsCount: Int
)

enum class NotificationType(val id: Int) {
    ALL_NOTIFICATIONS(0),
    NEW_COMMENTS(1),
    SYSTEM_MESSAGES(2),
    NEW_WORKS_FOR_LIKED_REQUESTS(3),
    HELPDESK_RESPONSES(4),
    TEXT_CHANGES_IN_OWN_FANFIC(5),
    NEW_PRESENTS(6),
    NEW_ACHIEVEMENTS(7),
    TEXT_CHANGES_IN_EDITED_FANFIC(8),
    ERROR_MESSAGES(9),
    REQUESTS_FOR_COAUTHORSHIPS(10),
    REQUESTS_FOR_BETA_EDITING(11),
    REQUESTS_FOR_GAMMA_EDITING(12),
    NEW_WORKS_ON_MY_REQUESTS(13),
    DISCUSSION_IN_COMMENTS(14),
    DISCUSSION_IN_REQUEST_COMMENTS(15),
    PRIVATE_MESSAGES(16),
    UPDATES_FROM_SUBSCRIBED_AUTHORS(17),
    NEW_WORKS_IN_COLLECTIONS(18),
    UPDATES_IN_FANFICS(19),
    NEW_COMMENTS_FOR_REQUEST(20),
    NEW_FANFICS_REWARDS(21),
    NEW_COMMENTS_REWARDS(22),
    CHANGES_IN_HEADER_OF_WORK(23),
    COAUTHOR_ADD_NEW_CHAPTER(24),
    NEW_BLOGS(25),
    UNKNOWN(-1);
}