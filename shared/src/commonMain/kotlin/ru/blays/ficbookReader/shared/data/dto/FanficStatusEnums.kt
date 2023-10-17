package ru.blays.ficbookReader.shared.data.dto

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