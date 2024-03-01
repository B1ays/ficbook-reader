package ru.blays.ficbook.api.dataModels

import kotlinx.serialization.Serializable

@Serializable
data class FandomModel(
    val href: String,
    val name: String,
    val description: String
) {
    override fun toString(): String {
        return """
href: $href
name: $name
description: $description
""".trimIndent() + "\n"
    }
}