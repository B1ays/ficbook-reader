package ru.blays.ficbookapi.dataModels

import kotlinx.serialization.Serializable

@Serializable
data class FanficPageModel(
    val id: String,
    val name: String,
    val status: FanficStatus,
    val author: List<UserModel>,
    val fandom: List<FandomModel>,
    val pairings: List<PairingModel>,
    val tags: List<FanficTag>,
    val coverUrl: CoverUrl,
    val description: String,
    val subscribersCount: Int,
    val commentCount: Int,
    val pagesCount: Int,
    val liked: Boolean,
    val subscribed: Boolean,
    val inCollectionsCount: Int,
    val chapters: List<FanficChapter>,
    val rewards: List<RewardModel>
) {
    override fun toString(): String {
        return """
id: $id
name: $name
status: $status
author: $author
fandom: $fandom
coverUrl: $coverUrl
tags: ${tags.joinToString { it.toString() }}
description: $description
chapters: ${chapters.joinToString("\n") { it.toString() }}
rewards: ${rewards.joinToString("\n") { it.toString() }}
""".trimIndent()
    }
}

@Serializable
sealed class FanficChapter {
    @Serializable
    data class SeparateChapterModel(
        val href: String,
        val name: String,
        val date: String,
        val commentsCount: Int,
        val commentsHref: String
    ): FanficChapter() {
        override fun toString(): String {
            return """
                href: $href
                name: $name
                date: $date
                commentsCount: $commentsCount
                commentsHref: $commentsHref
            """.trimIndent()
        }
    }

    @Serializable
    data class SingleChapterModel(
        val date: String,
        val commentsCount: Int,
        val commentsHref: String,
        val text: String
    ): FanficChapter()
}

@Serializable
data class RewardModel(
    val message: String,
    val fromUser: String,
    val awardDate: String
) {
    override fun toString(): String {
        return "$message от $fromUser"
    }
}

