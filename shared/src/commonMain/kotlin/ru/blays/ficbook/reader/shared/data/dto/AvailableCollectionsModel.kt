package ru.blays.ficbook.reader.shared.data.dto
import kotlinx.serialization.Serializable

@Serializable
data class AvailableCollectionsModel(
    val data: Data,
    val result: Boolean
) {
    @Serializable
    data class Data(
        val blacklisted: Boolean,
        val collections: List<Collection>
    ) {
        @Serializable
        data class Collection(
            val added: String,
            val authorId: Int,
            val count: Int,
            val description: String,
            val id: Int,
            val isInThisCollection: Int,
            val isPublic: Boolean,
            val lastUpdated: String,
            val name: String,
            val slug: String
        ) {
            companion object {
                const val IN_COLLECTION = 1
                const val NOT_IN_COLLECTION = 0

            }
        }
    }
}