package ru.blays.ficbookapi.dataModels

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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