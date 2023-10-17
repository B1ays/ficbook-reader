package ru.blays.ficbookapi.dataModels

import okhttp3.FormBody
import okhttp3.RequestBody

data class LoginModel(
    val login: String,
    val password: String,
    val remember: Boolean
) {
    fun toRequestBody(): RequestBody {
        return FormBody.Builder()
            .add("login", login)
            .add("password", password)
            .add("remember", remember.toString())
            .build()
    }
}