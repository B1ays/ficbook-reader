package ru.blays.ficbook.api.dataModels


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class AuthorizationResult(
    val responseResult: AuthorizationResponseModel,
    val user: UserModel
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