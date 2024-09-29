package ru.blays.ficbook.reader.shared.data

import kotlinx.serialization.Serializable
import ru.blays.ficbook.reader.shared.data.serializers.LongRangeSerializer

@Serializable
data class SearchParams(
    val searchOriginals: Boolean,
    val searchFanfics: Boolean,
    val pagesCountRange: IntRangeSimple,
    val withStatus: List<Int>,
    val withRating: List<Int>,
    val withDirection: List<Int>,
    val onlyTranslations: Boolean,
    val onlyPremium: Boolean,
    val likesRange: IntRangeSimple,
    val minRewards: Int,
    val minComments: Int,
    @Serializable(with = LongRangeSerializer::class)
    val dateRange: LongRange,
    val title: String,
    val filterReaded: Boolean,
    val sort: Int
) {
    companion object {
        /**
         * Status flags
         **/
        const val STATUS_IN_PROGRESS = 1
        const val STATUS_COMPLETED = 2
        const val STATUS_FROZEN = 3

        /**
         * Rating flags
         **/
        const val RATING_G = 5
        const val RATING_PG13 = 6
        const val RATING_R = 7
        const val RATING_NC17 = 8
        const val RATING_NC21 = 9

        /**
        * Direction flags
        **/
        const val DIRECTION_GEN = 1
        const val DIRECTION_HET = 2
        const val DIRECTION_SLASH = 3
        const val DIRECTION_FEMSLASH = 4
        const val DIRECTION_OTHER = 7
        const val DIRECTION_MIXED = 6
        const val DIRECTION_ARTICLE = 5

        /**
         * Tags flags
        **/
        const val TAGS_ANY_SELECTED = 1
        const val TAGS_ALL_SELECTED = 2

        /**
        * Sort flags
        **/
        const val SORT_BY_LIKES_COUNT = 1
        const val SORT_BY_COMMENTS_COUNT = 2
        const val SORT_BY_DATE_FROM_NEW = 3
        const val SORT_BY_DATE_FROM_OLD = 9
        const val SORT_BY_PAGES_COUNT = 4
        const val SORT_BY_REWARDS_COUNT = 8
        const val SORT_20_RANDOM = 5

        val Default = SearchParams(
            searchOriginals = true,
            searchFanfics = true,
            pagesCountRange = IntRangeSimple.EMPTY,
            withStatus = emptyList(),
            withRating = listOf(
                RATING_G,
                RATING_PG13,
                RATING_R,
                RATING_NC17,
                RATING_NC21
            ),
            withDirection = listOf(
                DIRECTION_GEN,
                DIRECTION_HET,
                DIRECTION_SLASH,
                DIRECTION_FEMSLASH,
                DIRECTION_OTHER,
                DIRECTION_MIXED,
                DIRECTION_ARTICLE,
            ),
            onlyTranslations = false,
            onlyPremium = false,
            likesRange = IntRangeSimple.EMPTY,
            minRewards = 0,
            minComments = 0,
            dateRange = LongRange.EMPTY,
            title = "",
            filterReaded = false,
            sort = SORT_BY_LIKES_COUNT
        )
    }
}

@Serializable
data class SearchedFandomModel(
    val title: String,
    val description: String,
    val fanficsCount: Int,
    val id: String
)

@Serializable
data class SearchedCharactersGroup(
    val fandomName: String,
    val characters: List<SearchedCharacterModel>
)

@Serializable
data class SearchedCharacterModel(
    val fandomId: String,
    val id: String,
    val name: String,
    val aliases: List<String>,
)

@Serializable
data class SearchedPairingModel(
    val characters: Set<Character>
) {
    @Serializable
    data class Character(
        val fandomId: String,
        val id: String,
        val name: String,
        val modifier: String = ""
    )
}

@Serializable
data class SearchedTagModel(
    val title: String,
    val description: String,
    val usageCount: Int,
    val isAdult: Boolean,
    val id: String
)

@Serializable
data class IntRangeSimple(
    val start: Int,
    val end: Int
) {
    companion object {
        val EMPTY = IntRangeSimple(0, 0)
    }
}

@Serializable
data class SearchParamsEntityShortcut(
    val idHex: String,
    val name: String,
    val description: String
)