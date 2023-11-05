package ru.blays.ficbookapi.dataModels

import kotlinx.serialization.Serializable

@Serializable
data class UserModel(
    val name: String,
    val href: String = "",
    val avatarUrl: String = ""
)