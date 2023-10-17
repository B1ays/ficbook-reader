package ru.blays.ficbookapi.dataModels

import kotlinx.serialization.Serializable

@Serializable
data class CollectionModel(
    val href: String,
    val name: String,
    val size: Int,
    val private: Boolean,
    val owner: UserModel
) {
    override fun toString(): String {
        return """
            href: $href
            name: $name
            size: $size
            private: $private
            owner: $owner
        """.trimIndent()
    }
}
