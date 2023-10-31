package ru.blays.ficbookReader.shared.data.mappers

import ru.blays.ficbookReader.shared.data.dto.*
import ru.blays.ficbookapi.data.Section
import ru.blays.ficbookapi.data.SectionWithQuery
import ru.blays.ficbookapi.dataModels.*
import ru.blays.ficbookapi.dataModels.FanficCompletionStatus
import ru.blays.ficbookapi.dataModels.FanficDirection
import ru.blays.ficbookapi.dataModels.FanficRating

fun CollectionModel.toStableModel() = CollectionModelStable(
    href = href,
    name = name,
    size = size,
    private = private,
    owner = UserModelStable(
        name = owner.name,
        avatarUrl = owner.avatarUrl,
        userID = owner.id
    )
)

fun FandomModel.toStableModel() = FandomModelStable(
    href = href,
    name = name,
    description = description
)

fun FanficCardModel.toStableModel() = FanficCardModelStable(
    href = href,
    title = title,
    status = status.toStableModel(),
    author = author,
    fandom = fandom.toStableModel(),
    updateDate = updateDate,
    readInfo = readInfo?.toStableModel(),
    tags = tags.map(FanficTag::toStableModel),
    description = description,
    coverUrl = coverUrl.url,
    pairings = pairings.map(PairingModel::toStableModel),
    size = size
)

fun FanficStatus.toStableModel() = FanficStatusStable(
    direction = direction.toStableModel(),
    rating = rating.toStableModel(),
    status = status.toStableModel(),
    hot = hot,
    likes = likes,
    trophies = trophies
)

fun FanficTag.toStableModel() = FanficTagStable(
    name = name,
    isAdult = isAdult,
    href = href
)

fun ReadBadgeModel.toStableModel() = ReadBadgeModelStable(
    readDate = readDate,
    hasUpdate = hasUpdate
)

fun FanficPageModel.toStableModel() = FanficPageModelStable(
    fanficID = id,
    name = name,
    coverUrl = coverUrl,
    description = description,
    subscribersCount = subscribersCount,
    commentCount = commentCount,
    pagesCount = pagesCount,
    liked = liked,
    subscribed = subscribed,
    inCollectionsCount = inCollectionsCount,
    status = status.toStableModel(),
    author = UserModelStable(name = author),
    fandoms = listOf(FandomModelStable(name = fandom)),
    tags = tags.map(FanficTag::toStableModel),
    chapters = chapters.map(FanficChapter::toStableModel),
    rewards = rewards.map(RewardModel::toStableModel),
)

fun FanficChapter.toStableModel(): FanficChapterStable = when(this) {
    is FanficChapter.SeparateChapterModel -> FanficChapterStable.SeparateChapterModel(
        href = href,
        name = name,
        date = date,
        commentsCount = commentsCount,
        commentsHref = commentsHref
    )
    is FanficChapter.SingleChapterModel -> FanficChapterStable.SingleChapterModel(
        date = date,
        commentsCount = commentsCount,
        commentsHref = commentsHref,
        text = text
    )
}

fun RewardModel.toStableModel() = RewardModelStable(
    message = message,
    fromUser = fromUser,
    awardDate = awardDate
)

fun LoginModel.toStableModel() = LoginModelStable(
    login = login,
    password = password,
    remember = remember
)

fun UserModel.toStableModel() = UserModelStable(
    name = name,
    userID = id,
    avatarUrl = avatarUrl
)

fun AuthorizationResult.toStableModel() = AuthorizationResultStable(
    responseResult = responseResult.toStableModel(),
    cookies = cookies.map(CookieModel::toStableModel)
)

fun AuthorizationResponseModel.toStableModel() = AuthorizationResponseModelStable(
    error = error
        ?.reason
        ?.let { reason ->
            AuthorizationResponseModelStable.Error(
                reason = reason
            )
        },
    result = success
)

fun CookieModel.toStableModel() = CookieModelStable(
    name = name,
    value = value
)

fun SectionWithQuery.toStableModel() = ru.blays.ficbookReader.shared.data.dto.SectionWithQuery(
    name = name,
    path = path,
    queryParameters = queryParameters
)

fun FanficDirection.toStableModel(): ru.blays.ficbookReader.shared.data.dto.FanficDirection = when(this) {
    FanficDirection.GEN -> ru.blays.ficbookReader.shared.data.dto.FanficDirection.GEN
    FanficDirection.HET -> ru.blays.ficbookReader.shared.data.dto.FanficDirection.HET
    FanficDirection.SLASH -> ru.blays.ficbookReader.shared.data.dto.FanficDirection.SLASH
    FanficDirection.FEMSLASH -> ru.blays.ficbookReader.shared.data.dto.FanficDirection.FEMSLASH
    FanficDirection.ARTICLE -> ru.blays.ficbookReader.shared.data.dto.FanficDirection.ARTICLE
    FanficDirection.MIXED -> ru.blays.ficbookReader.shared.data.dto.FanficDirection.MIXED
    FanficDirection.OTHER -> ru.blays.ficbookReader.shared.data.dto.FanficDirection.OTHER
    FanficDirection.UNKNOWN -> ru.blays.ficbookReader.shared.data.dto.FanficDirection.UNKNOWN
}

fun FanficRating.toStableModel(): ru.blays.ficbookReader.shared.data.dto.FanficRating = when(this) {
    FanficRating.G -> ru.blays.ficbookReader.shared.data.dto.FanficRating.G
    FanficRating.PG13 -> ru.blays.ficbookReader.shared.data.dto.FanficRating.PG13
    FanficRating.R -> ru.blays.ficbookReader.shared.data.dto.FanficRating.R
    FanficRating.NC17 -> ru.blays.ficbookReader.shared.data.dto.FanficRating.NC17
    FanficRating.NC21 -> ru.blays.ficbookReader.shared.data.dto.FanficRating.NC21
    FanficRating.UNKNOWN -> ru.blays.ficbookReader.shared.data.dto.FanficRating.UNKNOWN
}

fun FanficCompletionStatus.toStableModel(): ru.blays.ficbookReader.shared.data.dto.FanficCompletionStatus = when(this) {
    FanficCompletionStatus.IN_PROGRESS -> ru.blays.ficbookReader.shared.data.dto.FanficCompletionStatus.IN_PROGRESS
    FanficCompletionStatus.COMPLETE -> ru.blays.ficbookReader.shared.data.dto.FanficCompletionStatus.COMPLETE
    FanficCompletionStatus.FROZEN -> ru.blays.ficbookReader.shared.data.dto.FanficCompletionStatus.FROZEN
    FanficCompletionStatus.UNKNOWN -> ru.blays.ficbookReader.shared.data.dto.FanficCompletionStatus.UNKNOWN
}

fun Section.toStableModel() = Section(
    name = name,
    segments = segments
)

fun PairingModel.toStableModel() = PairingModelStable(
    character = character,
    href = href,
    isHighlighted = isHighlighted
)
