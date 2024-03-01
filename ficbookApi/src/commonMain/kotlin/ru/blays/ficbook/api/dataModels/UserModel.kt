package ru.blays.ficbook.api.dataModels

import kotlinx.serialization.Serializable

@Serializable
data class UserModel(
    val name: String,
    val href: String = "",
    val avatarUrl: String = ""
)