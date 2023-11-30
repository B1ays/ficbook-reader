package ru.blays.ficbookapi.dataModels

data class PopularAuthorModel(
    val user: UserModel,
    val position: Int,
    val subscribersInfo: String
)
