package ru.blays.ficbook.reader.feature.fanficDownloader

import org.intellij.lang.annotations.Language

@Suppress("XmlUnusedNamespaceDeclaration")
@Language("html")
internal fun createHtmlPageForContent(
    title: String,
    content: String,
): String {
    return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n" +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">\n" +
            "<head>\n" +
            "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
            "    <title>$title</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <h2>$title</h2>\n" +
            "    <div>\n" +
            "    $content\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>"
}

@Suppress("XmlUnusedNamespaceDeclaration")
@Language("html")
internal fun generateTitlePage(
    title: String,
    href: String,
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
    return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n" +
        "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">\n" +
        "<head>\n" +
        "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
        "    <title>$title</title>\n" +
        "</head>\n" +
        "<body>\n" +
        "    <h1 style=\"margin-bottom: 0;\">$title</h1>\n" +
        "    <a href=\"$href\">$href</a>\n" +
        "    <br/>\n" +
        "    <br/>\n" +
        "    <b>Направленность:</b> $direction <br/>\n" +
        "    <b>Авторы:</b> ${authors.joinToString()} <br/>\n" +
        "    <b>Фэндом:</b> ${fandoms.joinToString()} <br/>\n" +
        "    <b>Рейтинг:</b> $rating <br/>\n" +
        "    <b>Кол-во частей:</b> $chaptersCount <br/>\n" +
        "    <b>Статус:</b> $status <br/>\n" +
        "    <b>Метки:</b> ${tags.joinToString()} <br/><br/>\n" +
        "    <b>Описание:</b><br/>$description <br/><br/>\n" +
        "    <b>Публикация на других ресурсах: </b><br/> $publicationRules <br/><br/>\n" +
        if (authorComment != null) "    <b>Примечания:</b><br/> $authorComment\n" else "" +
        "</body>\n" +
        "</html>"
}

fun addHrefAttrForLinks(html: String): String {
    var processesHtml = html
    val matcher = Patterns.AUTOLINK_WEB_URL.matcher(html)
    matcher.group()
    val sequence = generateSequence { if(matcher.find()) matcher.start() to matcher.end() else null }
    sequence.forEach { (start, end) ->
        val link = html.substring(start, end)
        processesHtml = processesHtml.replaceRange(
            startIndex = start,
            endIndex = end,
            replacement = "<a href=\"$link\">$link</a>"
        )
    }
    return processesHtml
}