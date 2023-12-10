package ru.blays.ficbookReader.shared.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class SearchParams(
    val fandomsFilter: String,
    val fandomsGroup: Int,
    val pairings: Set<SearchedCharactersModel.SearchedCharactersItem>,
    val excludedPairings: Set<SearchedCharactersModel.SearchedCharactersItem>,
    val pagesCountRange: IntRangeSimple,
    val withStatus: List<Int>,
    val withRating: List<Int>,
    val withDirection: List<Int>,
    val translate: Int,
    val onlyPremium: Boolean,
    val likesRange: IntRangeSimple,
    val minRewards: Int,
    val dateRange: LongRange,
    val title: String,
    val filterReaded: Boolean,
    val sort: Int
) {
    companion object {

        /**
        * Fandom filter flags
        **/
        const val FANDOM_FILTER_ALL = "any"
        const val FANDOM_FILTER_ORIGINALS = "originals"
        const val FANDOM_FILTER_CATEGORY = "group"
        const val FANDOM_FILTER_CONCRETE = "fandom"

        /**
        * Fandoms group flags
        **/
        const val FANDOM_GROUP_ANIME_AND_MANGA = 1
        const val FANDOM_GROUP_BOOKS = 2
        const val FANDOM_GROUP_CARTOONS = 3
        const val FANDOM_GROUP_GAMES = 4
        const val FANDOM_GROUP_MOVIES = 5
        const val FANDOM_GROUP_OTHER = 6
        const val FANDOM_GROUP_RPF = 9
        const val FANDOM_GROUP_COMICS = 10
        const val FANDOM_GROUP_MUSICALS = 11

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
         * Translate flags
         **/
        const val TRANSLATE_DOESNT_MATTER = 1
        const val TRANSLATE_YES = 2
        const val TRANSLATE_NO = 3

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
        * Sort flags
        **/
        const val SORT_BY_LIKES_COUNT = 1
        const val SORT_BY_COMMENTS_COUNT = 2
        const val SORT_BY_DATE_FROM_NEW = 3
        const val SORT_BY_DATE_FROM_OLD = 9
        const val SORT_BY_PAGES_COUNT = 4
        const val SORT_BY_REWARDS_COUNT = 8
        const val SORT_20_RANDOM = 5

        val default get() = SearchParams(
            fandomsFilter = FANDOM_FILTER_ALL,
            fandomsGroup = FANDOM_GROUP_ANIME_AND_MANGA,
            pairings = emptySet(),
            excludedPairings = emptySet(),
            pagesCountRange = IntRangeSimple.EMPTY,
            withStatus = emptyList(),
            withRating = listOf(
                RATING_G,
                RATING_PG13,
                RATING_R,
                RATING_NC17,
                RATING_NC21
            ),
            withDirection = emptyList(),
            translate = TRANSLATE_DOESNT_MATTER,
            onlyPremium = false,
            likesRange = IntRangeSimple.EMPTY,
            minRewards = 0,
            dateRange = LongRange.EMPTY,
            title = "",
            filterReaded = false,
            sort = SORT_BY_LIKES_COUNT
        )
    }
}


@Serializable
data class SearchedFandomsModel(
    @SerialName("data")
    val data: Data,
    @SerialName("result")
    val result: Boolean
) {
    @Serializable
    data class Data(
        @SerialName("more")
        val more: Boolean,
        @SerialName("result")
        val result: List<Result>
    ) {
        @Serializable
        data class Result(
            @SerialName("fanfic_cnt")
            val fanficCnt: Int,
            @SerialName("group_crazy_title")
            val groupCrazyTitle: String,
            @SerialName("group_id")
            val groupId: Int,
            @SerialName("group_title")
            val groupTitle: GroupTitle?,
            @SerialName("id")
            val id: String,
            @SerialName("sec_title")
            val secTitle: String,
            @SerialName("slug")
            val slug: String,
            @SerialName("title")
            val title: String
        ) {
            @Serializable
            class GroupTitle
        }
    }
}

class SearchedCharactersModel : ArrayList<SearchedCharactersModel.SearchedCharactersItem>(){
    @Serializable
    data class SearchedCharactersItem(
        @SerialName("chars")
        val chars: List<Char>,
        @SerialName("date_added")
        val dateAdded: String,
        @SerialName("id")
        val id: Int,
        @SerialName("title")
        val title: String,
        @SerialName("user_id")
        val userId: Int
    ) {
        @Serializable
        data class Char(
            @SerialName("aliases")
            val aliases: List<String>,
            @SerialName("id")
            val id: Int,
            @SerialName("name")
            val name: String
        )
    }
}

@Serializable
data class SearchedTagsModel(
    @SerialName("data")
    val data: Data,
    @SerialName("result")
    val result: Boolean
) {
    @Serializable
    data class Data(
        @SerialName("more")
        val more: Boolean,
        @SerialName("tags")
        val tags: List<Tag>,
        @SerialName("total")
        val total: Int
    ) {
        @Serializable
        data class Tag(
            @SerialName("category")
            val category: String,
            @SerialName("category_highlight")
            val categoryHighlight: List<String>,
            @SerialName("description")
            val description: String,
            @SerialName("id")
            val id: String,
            @SerialName("isAdult")
            val isAdult: Boolean,
            @SerialName("isSpoiler")
            val isSpoiler: Boolean,
            @SerialName("slug")
            val slug: String,
            @SerialName("synonyms")
            val synonyms: List<Synonym>,
            @SerialName("synonyms_highlight")
            val synonymsHighlight: List<String>,
            @SerialName("title")
            val title: String,
            @SerialName("title_highlight")
            val titleHighlight: String,
            @SerialName("usage_count")
            val usageCount: Int
        ) {
            @Serializable
            data class Synonym(
                @SerialName("id")
                val id: Int,
                @SerialName("title")
                val title: String
            )
        }
    }
}

data class IntRangeSimple(
    val start: Int,
    val end: Int
) {
    companion object {
        val EMPTY = IntRangeSimple(0, 0)
    }
}