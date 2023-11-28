package ru.blays.ficbookapi.dataModels

data class LoginModel(
    val login: String,
    val password: String,
    val remember: Boolean
)