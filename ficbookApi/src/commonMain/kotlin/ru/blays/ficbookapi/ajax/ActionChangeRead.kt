package ru.blays.ficbookapi.ajax

import kotlinx.serialization.json.Json
import okhttp3.CookieJar
import okhttp3.FormBody
import ru.blays.ficbookapi.dataModels.AjaxSimpleResult
import ru.blays.ficbookapi.ficbookConnection.buildFicbookRequest
import ru.blays.ficbookapi.ficbookConnection.buildFicbookURL
import ru.blays.ficbookapi.ficbookConnection.href
import ru.blays.ficbookapi.getHtmlBody

suspend fun actionChangeRead(
    read: Boolean,
    cookieJar: CookieJar? = null,
    fanficID: String
): Boolean {
    val href = if (read) "/fanfic_read/read" else "/fanfic_read/unread"
    val url = buildFicbookURL {
        href(href)
    }
    val bodyBuilder = FormBody.Builder()
        .add("fanfic_id", fanficID)
        .build()

    val request = buildFicbookRequest {
        post(bodyBuilder)
        url(url)
    }
    val responseBody = getHtmlBody(
        request = request,
        cookieJar = cookieJar
    ).value
    return if (responseBody != null) {
        try {
            Json.decodeFromString<AjaxSimpleResult>(responseBody).result
        } catch (e: Exception) {
            false
        }
    } else {
        false
    }
}