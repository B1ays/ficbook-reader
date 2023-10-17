package ru.blays.ficbookapi.ajax

import kotlinx.serialization.json.Json
import okhttp3.FormBody
import ru.blays.ficbookapi.dataModels.AjaxSimpleResult
import ru.blays.ficbookapi.dataModels.CookieModel
import ru.blays.ficbookapi.ficbookConnection.buildFicbookRequest
import ru.blays.ficbookapi.ficbookConnection.buildFicbookURL
import ru.blays.ficbookapi.ficbookConnection.href
import ru.blays.ficbookapi.getHtmlBody

suspend fun actionChangeMark(
    mark: Boolean,
    cookies: List<CookieModel>,
    fanficID: String
): Boolean {
    val href = "/ajax/mark"
    val action = if(mark) "add" else "remove"
    val url = buildFicbookURL {
        href(href)
    }
    val bodyBuilder = FormBody.Builder()
        .add("fanfic_id", fanficID)
        .add("action", action)
        .build()

    val request = buildFicbookRequest(cookies) {
        post(bodyBuilder)
        header("Referer", "https://ficbook.net/readfic/$fanficID")
        url(url)
    }
    val responseBody = getHtmlBody(request)

    val result = try {
        Json.decodeFromString<AjaxSimpleResult>(responseBody!!)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    return result?.result ?: false
}