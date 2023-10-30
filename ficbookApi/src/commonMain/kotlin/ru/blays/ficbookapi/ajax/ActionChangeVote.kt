package ru.blays.ficbookapi.ajax

import kotlinx.serialization.json.Json
import okhttp3.FormBody
import ru.blays.ficbookapi.dataModels.AjaxSimpleResult
import ru.blays.ficbookapi.dataModels.CookieModel
import ru.blays.ficbookapi.ficbookConnection.buildFicbookRequest
import ru.blays.ficbookapi.ficbookConnection.buildFicbookURL
import ru.blays.ficbookapi.ficbookConnection.href
import ru.blays.ficbookapi.getHtmlBody

internal suspend fun actionChangeVote(
    vote: Boolean,
    cookies: List<CookieModel>,
    chapterHref: String
): Boolean {
    val href = if (vote) "/fanfics/continue_votes/add" else "/fanfics/continue_votes/remove"
    val url = buildFicbookURL {
        href(href)
    }
    val bodyBuilder = FormBody.Builder()
        .add("part_id", chapterHref.split("/").last())
        .build()

    val request = buildFicbookRequest(cookies) {
        post(bodyBuilder)
        header("Referer", "https://ficbook.net$chapterHref")
        url(url)
    }
    val responseBody = getHtmlBody(request).value

    val responseModel = responseBody?.let { Json.decodeFromString<AjaxSimpleResult>(it) }

    return responseModel?.result ?: false
}