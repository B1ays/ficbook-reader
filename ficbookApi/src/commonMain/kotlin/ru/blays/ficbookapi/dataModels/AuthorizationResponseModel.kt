package ru.blays.ficbookapi.dataModels


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class AuthorizationResult(
    val responseResult: AuthorizationResponseModel,
    val cookies: List<CookieModel>
)

@Serializable
data class AuthorizationResponseModel(
    @SerialName("error")
    val error: Error? = null,
    @SerialName("data")
    val data: Data? = null,
    @SerialName("result")
    val success: Boolean
) {
    @Serializable
    data class Error(
        @SerialName("reason")
        val reason: String
    )
    @Serializable
    data class Data(
        @SerialName("redirect")
        val redirect: String
    )
}