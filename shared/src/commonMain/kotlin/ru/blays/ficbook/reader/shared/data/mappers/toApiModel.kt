package ru.blays.ficbook.reader.shared.data.mappers

import ru.blays.ficbook.api.dataModels.CommentBlockModel
import ru.blays.ficbook.api.dataModels.CookieModel
import ru.blays.ficbook.api.dataModels.LoginModel
import ru.blays.ficbook.api.dataModels.QuoteModel
import ru.blays.ficbook.reader.shared.data.dto.*

fun LoginModelStable.toApiModel() = LoginModel(
    login = login,
    password = password,
    remember = remember
)

fun SectionWithQuery.toApiModel() = ru.blays.ficbook.api.data.SectionWithQuery(
    name = name,
    path = path,
    queryParameters = queryParameters
)

fun CookieModelStable.toApiModel() = CookieModel(
    name = name,
    value = value
)

fun NotificationType.toApiModel() = when(this) {
    NotificationType.ALL_NOTIFICATIONS -> ru.blays.ficbook.api.dataModels.NotificationType.ALL_NOTIFICATIONS
    NotificationType.NEW_COMMENTS -> ru.blays.ficbook.api.dataModels.NotificationType.NEW_COMMENTS
    NotificationType.SYSTEM_MESSAGES -> ru.blays.ficbook.api.dataModels.NotificationType.SYSTEM_MESSAGES
    NotificationType.NEW_WORKS_FOR_LIKED_REQUESTS -> ru.blays.ficbook.api.dataModels.NotificationType.NEW_WORKS_FOR_LIKED_REQUESTS
    NotificationType.HELPDESK_RESPONSES -> ru.blays.ficbook.api.dataModels.NotificationType.HELPDESK_RESPONSES
    NotificationType.TEXT_CHANGES_IN_OWN_FANFIC -> ru.blays.ficbook.api.dataModels.NotificationType.TEXT_CHANGES_IN_OWN_FANFIC
    NotificationType.NEW_PRESENTS -> ru.blays.ficbook.api.dataModels.NotificationType.NEW_PRESENTS
    NotificationType.NEW_ACHIEVEMENTS -> ru.blays.ficbook.api.dataModels.NotificationType.NEW_ACHIEVEMENTS
    NotificationType.TEXT_CHANGES_IN_EDITED_FANFIC -> ru.blays.ficbook.api.dataModels.NotificationType.TEXT_CHANGES_IN_EDITED_FANFIC
    NotificationType.ERROR_MESSAGES -> ru.blays.ficbook.api.dataModels.NotificationType.ERROR_MESSAGES
    NotificationType.REQUESTS_FOR_COAUTHORSHIPS -> ru.blays.ficbook.api.dataModels.NotificationType.REQUESTS_FOR_COAUTHORSHIPS
    NotificationType.REQUESTS_FOR_BETA_EDITING -> ru.blays.ficbook.api.dataModels.NotificationType.REQUESTS_FOR_BETA_EDITING
    NotificationType.REQUESTS_FOR_GAMMA_EDITING -> ru.blays.ficbook.api.dataModels.NotificationType.REQUESTS_FOR_GAMMA_EDITING
    NotificationType.NEW_WORKS_ON_MY_REQUESTS -> ru.blays.ficbook.api.dataModels.NotificationType.NEW_WORKS_ON_MY_REQUESTS
    NotificationType.DISCUSSION_IN_COMMENTS -> ru.blays.ficbook.api.dataModels.NotificationType.DISCUSSION_IN_COMMENTS
    NotificationType.DISCUSSION_IN_REQUEST_COMMENTS -> ru.blays.ficbook.api.dataModels.NotificationType.DISCUSSION_IN_REQUEST_COMMENTS
    NotificationType.PRIVATE_MESSAGES -> ru.blays.ficbook.api.dataModels.NotificationType.PRIVATE_MESSAGES
    NotificationType.UPDATES_FROM_SUBSCRIBED_AUTHORS -> ru.blays.ficbook.api.dataModels.NotificationType.UPDATES_FROM_SUBSCRIBED_AUTHORS
    NotificationType.NEW_WORKS_IN_COLLECTIONS -> ru.blays.ficbook.api.dataModels.NotificationType.NEW_WORKS_IN_COLLECTIONS
    NotificationType.UPDATES_IN_FANFICS -> ru.blays.ficbook.api.dataModels.NotificationType.UPDATES_IN_FANFICS
    NotificationType.NEW_COMMENTS_FOR_REQUEST -> ru.blays.ficbook.api.dataModels.NotificationType.NEW_COMMENTS_FOR_REQUEST
    NotificationType.NEW_FANFICS_REWARDS -> ru.blays.ficbook.api.dataModels.NotificationType.NEW_FANFICS_REWARDS
    NotificationType.NEW_COMMENTS_REWARDS -> ru.blays.ficbook.api.dataModels.NotificationType.NEW_COMMENTS_REWARDS
    NotificationType.CHANGES_IN_HEADER_OF_WORK -> ru.blays.ficbook.api.dataModels.NotificationType.CHANGES_IN_HEADER_OF_WORK
    NotificationType.COAUTHOR_ADD_NEW_CHAPTER -> ru.blays.ficbook.api.dataModels.NotificationType.COAUTHOR_ADD_NEW_CHAPTER
    NotificationType.NEW_BLOGS -> ru.blays.ficbook.api.dataModels.NotificationType.NEW_BLOGS
    NotificationType.UNKNOWN -> ru.blays.ficbook.api.dataModels.NotificationType.UNKNOWN
}

fun CommentBlockModelStable.toApiModel() = CommentBlockModel(
    text = text, quote = quote?.toApiModel()
)

fun QuoteModelStable.toApiModel(): QuoteModel {
    val includedQuote = quote?.toApiModel()
    return QuoteModel(
        text = text,
        quote = includedQuote,
        userName = userName
    )
}
