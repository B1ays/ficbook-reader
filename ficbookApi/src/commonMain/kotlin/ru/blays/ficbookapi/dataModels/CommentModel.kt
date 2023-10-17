package ru.blays.ficbookapi.dataModels

data class CommentModel(
    val avatarUrl: String,
    val authorName: String,
    val time: String,
    val text: String
)
