package ru.blays.ficbookapi.dataModels

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RewardResponseItem(
    @SerialName("date_added")
    val dateAdded: String,
    @SerialName("fanfic_id")
    val fanficId: Int,
    @SerialName("giver_id")
    val giverId: Int,
    @SerialName("id")
    val id: Int,
    @SerialName("receiver_id")
    val receiverId: Int,
    @SerialName("status")
    val status: Int,
    @SerialName("user_slug")
    val userSlug: String,
    @SerialName("user_text")
    val userText: String,
    @SerialName("username")
    val username: String
)