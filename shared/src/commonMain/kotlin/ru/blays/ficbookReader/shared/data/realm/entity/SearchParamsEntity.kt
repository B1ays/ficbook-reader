@file:Suppress("MemberVisibilityCanBePrivate", "PropertyName")

package ru.blays.ficbookReader.shared.data.realm.entity

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.realmSetOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmSet
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId
import ru.blays.ficbookReader.shared.data.dto.*

class SearchParamsEntity(): RealmObject {
    @PrimaryKey var _id: ObjectId = ObjectId()

    var name: String = ""
    var description: String = ""

    // Search params
    var fandomsFilter: String = ""
    var fandomsGroup: Int = 1
    var includedFandoms: RealmSet<FandomEntity> = realmSetOf()
    var excludedFandoms: RealmSet<FandomEntity> = realmSetOf()
    var excludeOtherFandoms: Boolean = false
    var includedPairings: RealmSet<PairingEntity> = realmSetOf()
    var excludedPairings: RealmSet<PairingEntity> = realmSetOf()
    var includedTags: RealmSet<TagEntity> = realmSetOf()
    var excludedTags: RealmSet<TagEntity> = realmSetOf()
    var pagesCountRange: IntRangeEntity? = null
    var withStatus: RealmList<Int> = realmListOf()
    var withRating: RealmList<Int> = realmListOf()
    var withDirection: RealmList<Int> = realmListOf()
    var translate: Int = 1
    var onlyPremium: Boolean = false
    var likesRange: IntRangeEntity? = null
    var minRewards: Int = 0
    var dateRange: LongRangeEntity? = null
    var title: String = ""
    var filterReaded: Boolean = false
    var sort: Int = 1

    constructor(
        name: String = "",
        description: String = "",

        fandomsFilter: String = "",
        fandomsGroup: Int = 1,
        includedFandoms: RealmSet<FandomEntity> = realmSetOf(),
        excludedFandoms: RealmSet<FandomEntity> = realmSetOf(),
        excludeOtherFandoms: Boolean = false,
        includedPairings: RealmSet<PairingEntity> = realmSetOf(),
        excludedPairings: RealmSet<PairingEntity> = realmSetOf(),
        includedTags: RealmSet<TagEntity> = realmSetOf(),
        excludedTags: RealmSet<TagEntity> = realmSetOf(),
        pagesCountRange: IntRangeEntity = IntRangeEntity(),
        withStatus: RealmList<Int> = realmListOf(),
        withRating: RealmList<Int> = realmListOf(),
        withDirection: RealmList<Int> = realmListOf(),
        translate: Int = 1,
        onlyPremium: Boolean = false,
        likesRange: IntRangeEntity = IntRangeEntity(),
        minRewards: Int = 0,
        dateRange: LongRangeEntity = LongRangeEntity(),
        title: String = "",
        filterReaded: Boolean = false,
        sort: Int = 1
    ): this() {
        this.name = name
        this.description = description

        this.fandomsFilter = fandomsFilter
        this.fandomsGroup = fandomsGroup
        this.includedFandoms = includedFandoms
        this.excludedFandoms = excludedFandoms
        this.excludeOtherFandoms = excludeOtherFandoms
        this.includedPairings = includedPairings
        this.excludedPairings = excludedPairings
        this.includedTags = includedTags
        this.excludedTags = excludedTags
        this.pagesCountRange = pagesCountRange
        this.withStatus = withStatus
        this.withRating = withRating
        this.withDirection = withDirection
        this.translate = translate
        this.onlyPremium = onlyPremium
        this.likesRange = likesRange
        this.minRewards = minRewards
        this.dateRange = dateRange
        this.title = title
        this.filterReaded = filterReaded
        this.sort = sort
    }
    constructor(
        name: String,
        description: String = "",
        searchParams: SearchParams,
        includedFandoms: Set<SearchedFandomModel> = emptySet(),
        excludedFandoms: Set<SearchedFandomModel> = emptySet(),
        includedPairings: Set<SearchedPairingModel> = emptySet(),
        excludedPairings: Set<SearchedPairingModel> = emptySet(),
        includedTags: Set<SearchedTagModel> = emptySet(),
        excludedTags: Set<SearchedTagModel> = emptySet(),
    ): this() {
        this.name = name
        this.description = description

        // Search params
        this.fandomsFilter = searchParams.fandomsFilter
        this.fandomsGroup = searchParams.fandomsGroup
        this.includedFandoms = includedFandoms.mapTo(realmSetOf()) {
            FandomEntity(
                id = it.id,
                title = it.title,
                description = it.description,
                fanficsCount = it.fanficsCount
            )
        }
        this.excludedFandoms = excludedFandoms.mapTo(realmSetOf()) {
            FandomEntity(
                id = it.id,
                title = it.title,
                description = it.description,
                fanficsCount = it.fanficsCount
            )
        }
        this.includedPairings = includedPairings.mapTo(realmSetOf()) {
            PairingEntity(characters = it.characters)
        }
        this.excludedPairings = excludedPairings.mapTo(realmSetOf()) {
            PairingEntity(characters = it.characters)
        }
        this.includedTags = includedTags.mapTo(realmSetOf()) {
            TagEntity(
                id = it.id,
                title = it.title,
                description = it.description,
                usageCount = it.usageCount,
                isAdult = it.isAdult
            )
        }
        this.excludedTags = excludedTags.mapTo(realmSetOf()) {
            TagEntity(
                id = it.id,
                title = it.title,
                description = it.description,
                usageCount = it.usageCount,
                isAdult = it.isAdult
            )
        }
        this.pagesCountRange = searchParams.pagesCountRange.let {
            IntRangeEntity(
                start = it.start,
                end = it.end
            )
        }
        this.withStatus = searchParams.withStatus.toRealmList()
        this.withRating = searchParams.withRating.toRealmList()
        this.withDirection = searchParams.withDirection.toRealmList()
        this.translate = searchParams.translate
        this.onlyPremium = searchParams.onlyPremium
        this.likesRange = searchParams.likesRange.let {
            IntRangeEntity(
                start = it.start,
                end = it.end
            )
        }
        this.minRewards = searchParams.minRewards
        this.dateRange = searchParams.dateRange.let {
            LongRangeEntity(
                start = it.first,
                end = it.last
            )
        }
        this.title = searchParams.title
        this.filterReaded = searchParams.filterReaded
        this.sort = searchParams.sort
    }
}

class FandomEntity(): RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var id: String = ""
    var title: String = ""
    var description: String = ""
    var fanficsCount: Int = 0

    constructor(
        id: String = "",
        title: String = "",
        description: String = "",
        fanficsCount: Int = 0
    ): this() {
        this.id = id
        this.title = title
        this.description = description
        this.fanficsCount = fanficsCount
    }
}

class PairingEntity(): RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var characters: RealmSet<CharacterEntity> = realmSetOf()

    class CharacterEntity(): RealmObject {
        @PrimaryKey
        var _id: ObjectId = ObjectId()
        var fandomId: String = ""
        var id: String = ""
        var name: String = ""
        var modifier: String = ""

        constructor(
            fandomId: String = "",
            id: String = "",
            name: String = "",
            modifier: String = ""
        ): this() {
            this.fandomId = fandomId
            this.id = id
            this.name = name
            this.modifier = modifier
        }
    }

    constructor(
        characters: Set<SearchedPairingModel.Character>
    ): this() {
        this.characters = characters.mapTo(realmSetOf()) {
            CharacterEntity(
                it.fandomId,
                it.id,
                it.name,
                it.modifier
            )
        }
    }
    constructor(
        characters: RealmSet<CharacterEntity>
    ): this() {
        this.characters = characters
    }
}

class TagEntity(): RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var id: String = ""
    var title: String = ""
    var description: String = ""
    var usageCount: Int = 0
    var isAdult: Boolean = false

    constructor(
        id: String = "",
        title: String = "",
        description: String = "",
        usageCount: Int = 0,
        isAdult: Boolean = false
    ): this() {
        this.id = id
        this.title = title
        this.description = description
        this.usageCount = usageCount
        this.isAdult = isAdult
    }
}

class IntRangeEntity(): RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var start: Int = 0
    var end: Int = 0

    constructor(
        start: Int = 0,
        end: Int = 0
    ): this() {
        this.start = start
        this.end = end
    }
}

class LongRangeEntity(): RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var start: Long = 0
    var end: Long = 0

    constructor(
        start: Long = 0,
        end: Long = 0
    ): this() {
        this.start = start
        this.end = end
    }
}

fun FandomEntity.toDtoModel(): SearchedFandomModel {
    return SearchedFandomModel(
        id = id,
        title = title,
        description = description,
        fanficsCount = fanficsCount
    )
}

fun PairingEntity.toDtoModel(): SearchedPairingModel {
    return SearchedPairingModel(
        characters = characters.mapTo(mutableSetOf()) {
            SearchedPairingModel.Character(
                it.fandomId,
                it.id,
                it.name,
                it.modifier
            )
        }
    )
}

fun TagEntity.toDtoModel(): SearchedTagModel {
    return SearchedTagModel(
        id = id,
        title = title,
        description = description,
        usageCount = usageCount,
        isAdult = isAdult
    )
}

fun SearchParamsEntity.toShortcut() = SearchParamsEntityShortcut(
    idHex = _id.toHexString(),
    name = name,
    description = description
)