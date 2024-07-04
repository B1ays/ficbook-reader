package ru.blays.ficbook.reader.shared.data.mappers

import ru.blays.ficbook.api.data.Section
import ru.blays.ficbook.api.data.SectionWithQuery
import ru.blays.ficbook.api.dataModels.*
import ru.blays.ficbook.api.dataModels.AvailableCollectionsModel
import ru.blays.ficbook.api.dataModels.FanficCompletionStatus
import ru.blays.ficbook.api.dataModels.FanficDirection
import ru.blays.ficbook.api.dataModels.FanficRating
import ru.blays.ficbook.reader.shared.data.SearchedCharacterModel
import ru.blays.ficbook.reader.shared.data.SearchedCharactersGroup
import ru.blays.ficbook.reader.shared.data.SearchedFandomModel
import ru.blays.ficbook.reader.shared.data.SearchedTagModel
import ru.blays.ficbook.reader.shared.data.dto.*
import ru.blays.ficbook.reader.shared.data.dto.FanficShortcut
import ru.blays.ficbook.reader.shared.data.dto.NotificationType

fun CollectionCardModel.toStableModel() = when(this) {
    is CollectionCardModel.Own -> CollectionCardModelStable.Own(
        relativeID = relativeID,
        realID = realID,
        name = name,
        size = size,
        public = public,
    )
    is CollectionCardModel.Other -> CollectionCardModelStable.Other(
        relativeID = relativeID,
        realID = realID,
        name = name,
        size = size,
        owner = owner.toStableModel(),
        subscribed = subscribed
    )
}

fun CollectionPageModel.toStableModel() = when(this) {
    is CollectionPageModel.Own -> CollectionPageModelStable.Own(
        name = name,
        description = description,
        filterParams = filterParams.toStableModel()
    )
    is CollectionPageModel.Other -> CollectionPageModelStable.Other(
        name = name,
        description = description,
        owner = owner.toStableModel(),
        subscribed = subscribed,
        filterParams = filterParams.toStableModel()
    )
}

fun FandomModel.toStableModel() = FandomModelStable(
    href = href,
    name = name,
    description = description
)

fun FanficCardModel.toStableModel() = FanficCardModelStable(
    href = href,
    id = id,
    title = title,
    status = status.toStableModel(),
    author = author.toStableModel(),
    originalAuthor = originalAuthor?.toStableModel(),
    fandom = fandom.map(FandomModel::toStableModel),
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

fun FanficPageModel.toStableModel(
    chapters: FanficChapterStable
) = FanficPageModelStable(
    fanficID = id,
    name = name,
    coverUrl = coverUrl.url,
    description = description,
    dedication = dedication,
    authorComment = authorComment,
    publicationRules = publicationRules,
    subscribersCount = subscribersCount,
    commentCount = commentCount,
    pagesCount = pagesCount,
    liked = liked,
    subscribed = subscribed,
    inCollectionsCount = inCollectionsCount,
    status = status.toStableModel(),
    authors = authors.map(FanficAuthorModel::toStableModel),
    fandoms = fandoms.map(FandomModel::toStableModel),
    pairings = pairings.map(PairingModel::toStableModel),
    tags = tags.map(FanficTag::toStableModel),
    chapters = chapters,
    rewards = rewards.map(RewardModel::toStableModel),
)

fun FanficChapter.toStableModel(): FanficChapterStable = when(this) {
    is FanficChapter.SeparateChaptersModel -> FanficChapterStable.SeparateChaptersModel(
        chapters = chapters.map(FanficChapter.SeparateChaptersModel.Chapter::toStableModel),
        chaptersCount = chaptersCount
    )
    is FanficChapter.SingleChapterModel -> FanficChapterStable.SingleChapterModel(
        chapterID = chapterID,
        date = date,
        text = text
    )
}

fun FanficChapter.SeparateChaptersModel.Chapter.toStableModel() = FanficChapterStable.SeparateChaptersModel.Chapter(
    chapterID = chapterID,
    href = href,
    name = name,
    date = date,
    commentsCount = commentsCount
)

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
    href = href,
    avatarUrl = avatarUrl
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

fun SectionWithQuery.toStableModel() = ru.blays.ficbook.reader.shared.data.dto.SectionWithQuery(
    name = name,
    path = path,
    queryParameters = queryParameters
)

fun FanficDirection.toStableModel(): ru.blays.ficbook.reader.shared.data.dto.FanficDirection = when(this) {
    FanficDirection.GEN -> ru.blays.ficbook.reader.shared.data.dto.FanficDirection.GEN
    FanficDirection.HET -> ru.blays.ficbook.reader.shared.data.dto.FanficDirection.HET
    FanficDirection.SLASH -> ru.blays.ficbook.reader.shared.data.dto.FanficDirection.SLASH
    FanficDirection.FEMSLASH -> ru.blays.ficbook.reader.shared.data.dto.FanficDirection.FEMSLASH
    FanficDirection.ARTICLE -> ru.blays.ficbook.reader.shared.data.dto.FanficDirection.ARTICLE
    FanficDirection.MIXED -> ru.blays.ficbook.reader.shared.data.dto.FanficDirection.MIXED
    FanficDirection.OTHER -> ru.blays.ficbook.reader.shared.data.dto.FanficDirection.OTHER
    FanficDirection.UNKNOWN -> ru.blays.ficbook.reader.shared.data.dto.FanficDirection.UNKNOWN
}

fun FanficRating.toStableModel(): ru.blays.ficbook.reader.shared.data.dto.FanficRating = when(this) {
    FanficRating.G -> ru.blays.ficbook.reader.shared.data.dto.FanficRating.G
    FanficRating.PG13 -> ru.blays.ficbook.reader.shared.data.dto.FanficRating.PG13
    FanficRating.R -> ru.blays.ficbook.reader.shared.data.dto.FanficRating.R
    FanficRating.NC17 -> ru.blays.ficbook.reader.shared.data.dto.FanficRating.NC17
    FanficRating.NC21 -> ru.blays.ficbook.reader.shared.data.dto.FanficRating.NC21
    FanficRating.UNKNOWN -> ru.blays.ficbook.reader.shared.data.dto.FanficRating.UNKNOWN
}

fun FanficCompletionStatus.toStableModel(): ru.blays.ficbook.reader.shared.data.dto.FanficCompletionStatus = when(this) {
    FanficCompletionStatus.IN_PROGRESS -> ru.blays.ficbook.reader.shared.data.dto.FanficCompletionStatus.IN_PROGRESS
    FanficCompletionStatus.COMPLETE -> ru.blays.ficbook.reader.shared.data.dto.FanficCompletionStatus.COMPLETE
    FanficCompletionStatus.FROZEN -> ru.blays.ficbook.reader.shared.data.dto.FanficCompletionStatus.FROZEN
    FanficCompletionStatus.UNKNOWN -> ru.blays.ficbook.reader.shared.data.dto.FanficCompletionStatus.UNKNOWN
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

fun AuthorProfileModel.toStableModel() = AuthorProfileModelStable(
    authorMain = authorMain.toStableModel(),
    authorInfo = authorInfo.toStableModel(),
    availableTabs = availableTabs
)

fun AuthorMainInfo.toStableModel() = AuthorMainInfoStable(
    name = name,
    realID = realID,
    relativeID = relativeID,
    avatarUrl = avatarUrl,
    profileCoverUrl = profileCoverUrl,
    subscribers = subscribers,
    subscribed = subscribed
)

fun AuthorInfoModel.toStableModel() = AuthorInfoModelStable(
    about = about,
    contacts = contacts,
    support = support
)

fun BlogPostCardModel.toStableModel() = BlogPostCardModelStable(
    id = id,
    title = title,
    date = date,
    text = text,
    likes = likes
)

fun BlogPostPageModel.toStableModel() = BlogPostModelStable(
    title = title,
    date = date,
    text = text,
    likes = likes
)

fun AuthorPresentModel.toStableModel() = AuthorPresentModelStable(
    pictureUrl = pictureUrl,
    text = text,
    user = user.toStableModel()
)

fun AuthorFanficPresentModel.toStableModel() = AuthorFanficPresentModelStable(
    pictureUrl = pictureUrl,
    text = text,
    user = user.toStableModel(),
    forWork = forWork.toStableModel()
)

fun AuthorCommentPresentModel.toStableModel() = AuthorCommentPresentModelStable(
    pictureUrl = pictureUrl,
    text = text,
    user = user.toStableModel(),
    forWork = forWork.toStableModel()
)

fun CommentModel.toStableModel() = CommentModelStable(
    commentID = commentID,
    user = user.toStableModel(),
    isOwnComment = isOwnComment,
    date = date,
    blocks = blocks.map(CommentBlockModel::toStableModel),
    likes = likes,
    forFanfic = forFanfic?.toStableModel()
)

fun CommentBlockModel.toStableModel() = CommentBlockModelStable(
    quote = quote?.toStableModel(),
    text = text
)

fun QuoteModel.toStableModel(): QuoteModelStable {
    val quote = quote?.toStableModel()
    return QuoteModelStable(
        quote = quote,
        userName = userName,
        text = text
    )
}

fun ru.blays.ficbook.api.dataModels.FanficShortcut.toStableModel() = FanficShortcut(
    name = name,
    href = href
)

fun AvailableCollectionsModel.toStableModel() = ru.blays.ficbook.reader.shared.data.dto.AvailableCollectionsModel(
    data = data.toStableModel(),
    result = result
)

fun AvailableCollectionsModel.Data.toStableModel() = ru.blays.ficbook.reader.shared.data.dto.AvailableCollectionsModel.Data(
    blacklisted = blacklisted,
    collections = collections.map(AvailableCollectionsModel.Data.Collection::toStableModel)
)

fun AvailableCollectionsModel.Data.Collection.toStableModel() = ru.blays.ficbook.reader.shared.data.dto.AvailableCollectionsModel.Data.Collection(
    added = added,
    authorId = authorId,
    count = count,
    description = description,
    id = id,
    isInThisCollection = isInThisCollection,
    isPublic = isPublic,
    lastUpdated = lastUpdated,
    name = name,
    slug = slug
)

fun CollectionFilterParams.toStableModel() = CollectionFilterParamsStable(
    availableDirections = availableDirections,
    availableFandoms = availableFandoms,
    availableSortParams = availableSortParams
)

fun PopularAuthorModel.toStableModel() = PopularAuthorModelStable(
    user = user.toStableModel(),
    position = position,
    subscribersInfo = subscribersInfo
)

fun AuthorSearchResult.Data.Result.toUserModel() = UserModelStable(
    name = username,
    href = "authors/$id",
    avatarUrl = avatarPath
)

fun NotificationModel.toStableModel() = NotificationModelStable(
    href = href,
    title = title,
    date = date,
    text = text,
    readed = readed,
    type = type.toStableModel()
)

fun ru.blays.ficbook.api.dataModels.NotificationType.toStableModel() = NotificationType.valueOf(this.name)

fun NotificationCategory.toStableModel() = NotificationCategoryStable(
    type = type.toStableModel(),
    notificationsCount = notificationsCount
)

fun SearchedFandomsModel.Data.Result.toStableModel() = SearchedFandomModel(
    title = title,
    description = secTitle,
    fanficsCount = fanficCnt,
    id = id
)

fun SearchedCharactersModel.Data.toStableModel() = SearchedCharactersGroup(
    fandomName = title,
    characters = chars.map {
        it.toStableModel("$id")
    }
)

fun SearchedCharactersModel.Data.Char.toStableModel(fandomId: String) = SearchedCharacterModel(
    fandomId = fandomId,
    id = "$id",
    name = name,
    aliases = aliases
)

fun SearchedTagsModel.Data.Tag.toStableModel() = SearchedTagModel(
    title = title,
    description = description,
    usageCount = usageCount,
    isAdult = isAdult,
    id = id
)

fun FanficAuthorModel.toStableModel() = FanficAuthorModelStable(
    user = user.toStableModel(),
    role = role
)

fun FanficQuickActionsModel.Data.toStableModel() = FanficQuickActionsInfoModel(
    liked = isLiked,
    subscribed = isFollowed,
    readed = isFullyRead
)
