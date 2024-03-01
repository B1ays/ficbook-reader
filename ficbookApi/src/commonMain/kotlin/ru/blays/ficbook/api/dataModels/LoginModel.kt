package ru.blays.ficbook.api.dataModels

data class LoginModel(
    val login: String,
    val password: String,
    val remember: Boolean
)