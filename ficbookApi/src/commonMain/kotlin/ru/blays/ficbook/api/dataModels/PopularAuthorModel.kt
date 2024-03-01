package ru.blays.ficbook.api.dataModels

data class PopularAuthorModel(
    val user: UserModel,
    val position: Int,
    val subscribersInfo: String
)