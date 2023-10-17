package ru.blays.ficbookapi.dataModels

import kotlinx.serialization.Serializable

@Serializable
data class FanficModel(
    val href: String,
    val title: String,
    val status: FanficStatus,
    val author: String,
    val fandom: String,
    val updateDate: String,
    val readInfo: ReadBadgeModel?,
    val tags: List<FanficTag>,
    val description: String,
    val coverUrl: String
) {
    override fun toString(): String {
        return """
}
    href = $href
    title = $title
    status = $status
    author = $author
    fandom = $fandom
    updateDate = $updateDate
    tags = $tags
    description = $description
}""".trimIndent()
    }
}

@Serializable
data class FanficStatus(
    val direction: FanficDirection,
    val rating: FanficRating,
    val status: FanficCompletionStatus,
    val hot: Boolean,
    val likes: Int,
    val trophies: Int
) {
    override fun toString(): String {
        return """
            direction: $direction
            rating: $rating
            status: $status
            likes: $likes
            trophies: $trophies
        """.trimIndent()
    }
}

@Serializable
data class FanficTag(
    val name: String,
    val isAdult: Boolean,
    val href: String = ""
) {
    override fun toString(): String {
        return name + if (isAdult) " | 18+" else ""
    }
}

@Serializable
data class ReadBadgeModel(
    val readDate: String,
    val hasUpdate: Boolean
)

enum class FanficDirection(val direction: String) {
    GEN("Джен"),
    HET("Гет"),
    SLASH("Слэш"),
    FEMSLASH("Фемслэш"),
    ARTICLE("Статья"),
    MIXED("Смешанная"),
    OTHER("Другие виды отношений"),
    UNKNOWN("");

    companion object {
        fun getForName(name: String): FanficDirection {
            return FanficDirection.entries
                .firstOrNull { it.direction == name }
                ?: UNKNOWN
        }
    }
}

enum class FanficRating(val rating: String) {
    G("G"),
    PG13("PG-13"),
    R("R"),
    NC17("NC-17"),
    NC21("NC-21"),
    UNKNOWN("");

    companion object {
        fun getForName(name: String): FanficRating {
            return FanficRating.entries
                .firstOrNull { it.rating == name }
                ?: UNKNOWN
        }
    }
}

enum class FanficCompletionStatus(val status: String) {
    IN_PROGRESS("В процессе"),
    COMPLETE("Завершён"),
    FROZEN("Заморожен"),
    UNKNOWN("");

    companion object {
        fun getForName(name: String): FanficCompletionStatus {
            return FanficCompletionStatus.entries
                .firstOrNull { it.status == name }
                ?: UNKNOWN
        }
    }
}
