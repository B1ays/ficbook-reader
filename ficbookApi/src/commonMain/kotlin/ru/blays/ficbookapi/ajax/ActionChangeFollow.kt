package ru.blays.ficbookapi.ajax

import kotlinx.serialization.json.Json
import okhttp3.FormBody
import ru.blays.ficbookapi.dataModels.AjaxSimpleResult
import ru.blays.ficbookapi.dataModels.CookieModel
import ru.blays.ficbookapi.ficbookConnection.buildFicbookRequest
import ru.blays.ficbookapi.ficbookConnection.buildFicbookURL
import ru.blays.ficbookapi.ficbookConnection.href
import ru.blays.ficbookapi.getHtmlBody

suspend fun actionChangeFollow(
    follow: Boolean,
    cookies: List<CookieModel>,
    fanficID: String
): Boolean {
    val href = if (follow) "/fanfic_follow/follow" else "/fanfic_follow/unfollow"
    val url = buildFicbookURL {
        href(href)
    }
    val bodyBuilder = FormBody.Builder()
        .add("fanfic_id", fanficID)
        .build()

    val request = buildFicbookRequest(cookies) {
        post(bodyBuilder)
        header("Referer", "https://ficbook.net/readfic/$fanficID")
        url(url)
    }
    val responseBody = getHtmlBody(request)

    val responseModel = try {
        Json.decodeFromString<AjaxSimpleResult>(responseBody!!)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    return responseModel?.result ?: false
}