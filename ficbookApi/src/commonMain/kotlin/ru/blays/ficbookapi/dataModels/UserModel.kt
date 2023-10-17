package ru.blays.ficbookapi.dataModels

import kotlinx.serialization.Serializable

@Serializable
data class UserModel(
    val name: String,
    val id: String = "",
    val avatarUrl: String = ""
)