package ru.blays.ficbook.api.dataModels

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FanficQuickActionsModel(
    @SerialName("data")
    val data: Data,
    @SerialName("result")
    val result: Boolean
) {
    @Serializable
    data class Data(
        @SerialName("disableVotes")
        val disableVotes: Boolean,
        @SerialName("isAuthor")
        val isAuthor: Boolean,
        @SerialName("isFollowed")
        val isFollowed: Boolean,
        @SerialName("isFullyRead")
        val isFullyRead: Boolean,
        @SerialName("isLiked")
        val isLiked: Boolean
    )
}