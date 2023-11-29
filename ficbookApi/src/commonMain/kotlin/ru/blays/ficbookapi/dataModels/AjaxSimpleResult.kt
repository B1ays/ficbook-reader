package ru.blays.ficbookapi.dataModels

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class AjaxSimpleResult(
    @SerialName("result")
    val result: Boolean,
    @SerialName("message")
    val message: String = "",
    @SerialName("error")
    val error: String = ""
)