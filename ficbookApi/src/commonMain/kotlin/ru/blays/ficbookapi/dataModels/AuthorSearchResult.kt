package ru.blays.ficbookapi.dataModels


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthorSearchResult(
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
            @SerialName("avatar_decoration_style")
            val avatarDecorationStyle: String,
            @SerialName("avatar_path")
            val avatarPath: String,
            @SerialName("id")
            val id: String,
            @SerialName("is_premium")
            val isPremium: Boolean,
            @SerialName("slug")
            val slug: String,
            @SerialName("text")
            val text: String,
            @SerialName("username")
            val username: String
        )
    }
}