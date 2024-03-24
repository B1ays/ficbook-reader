package ru.blays.ficbook.reader.feature.fanficDownloader

internal fun generateDescription(
    title: String,
    link: String,
    direction: String,
    authors: List<String>,
    fandoms: List<String>,
    rating: String,
    chaptersCount: Int,
    status: String,
    tags: List<String>,
    description: String,
    publicationRules: String,
    authorComment: String?,
): String {
    return "    $title" + "\n\n" +
        "    Ссылка: $link" + "\n\n" +
        "    Направленность: $direction" + "\n\n" +
        "    Авторы: ${authors.joinToString()}" + "\n\n" +
        "    Фэндом: ${fandoms.joinToString()}" + "\n\n" +
        "    Рейтинг: $rating" + "\n\n" +
        "    Кол-во частей: $chaptersCount" + "\n\n" +
        "    Статус: $status" + "\n\n" +
        "    Теги: ${tags.joinToString()}" + "\n\n" +
        "    Описание:\n$description" + "\n\n" +
        "    Публиеация на других ресурсах:\n$publicationRules" + "\n\n" +
        if(authorComment != null) "    Примечания:\n$authorComment\n\n" else "\n\n"
}

internal fun addTitleForChapter(
    text: String,
    title: String
): String {
    return "\n    ==== $title ====\n\n$text"
}