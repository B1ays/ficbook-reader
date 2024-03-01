package ru.blays.ficbook.reader.shared.data.dto

import kotlinx.serialization.SerialName

data class AuthorizationResultStable(
    val responseResult: AuthorizationResponseModelStable,
    val cookies: List<CookieModelStable>
)

data class AuthorizationResponseModelStable(
    val error: Error? = null,
    val result: Boolean
) {
    data class Error(
        @SerialName("reason")
        val reason: String
    )
}

data class CookieModelStable(
    val name: String,
    val value: String
)