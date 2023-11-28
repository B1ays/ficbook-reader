package ru.blays.ficbookapi.dataModels
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AvailableCollectionsModel(
    @SerialName("data")
    val data: Data,
    @SerialName("result")
    val result: Boolean
) {
    @Serializable
    data class Data(
        @SerialName("blacklisted")
        val blacklisted: Boolean,
        @SerialName("collections")
        val collections: List<Collection>
    ) {
        @Serializable
        data class Collection(
            @SerialName("added")
            val added: String,
            @SerialName("author_id")
            val authorId: Int,
            @SerialName("count")
            val count: Int,
            @SerialName("description")
            val description: String,
            @SerialName("id")
            val id: Int,
            @SerialName("is_in_this_collection")
            val isInThisCollection: Int,
            @SerialName("is_public")
            val isPublic: Boolean,
            @SerialName("last_updated")
            val lastUpdated: String,
            @SerialName("name")
            val name: String,
            @SerialName("slug")
            val slug: String
        )
    }
}