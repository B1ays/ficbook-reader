package ru.blays.ficbook.api.dataModels

import kotlinx.serialization.Serializable

@Serializable
data class CookieModel(
    val name: String,
    val value: String
) {
    fun toHttpFormat(): String {
        return "$name=$value"
    }
}