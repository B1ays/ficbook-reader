package ru.blays.ficbookapi.api

import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import ru.blays.ficbookapi.data.SectionWithQuery
import ru.blays.ficbookapi.dataModels.FanficCardModel
import ru.blays.ficbookapi.dataModels.ListResult
import ru.blays.ficbookapi.ficbookExtensions.ficbookUrl
import ru.blays.ficbookapi.okHttpDsl.*
import ru.blays.ficbookapi.parsers.FanficCardParser
import ru.blays.ficbookapi.parsers.FanficsListParser
import ru.blays.ficbookapi.parsers.checkPageButtonsExists
import ru.blays.ficbookapi.result.ApiResult

interface FanficsListApi {
    suspend fun get(section: SectionWithQuery, page: Int): ApiResult<ListResult<FanficCardModel>>
    suspend fun get(href: String, page: Int): ApiResult<ListResult<FanficCardModel>>
}

class FanficsListApiImpl(
    private val client: OkHttpClient
): FanficsListApi {
    private val fanficsListParser = FanficsListParser()
    private val fanficCardParser = FanficCardParser()
    override suspend fun get(
        section: SectionWithQuery,
        page: Int
    ): ApiResult<ListResult<FanficCardModel>> = coroutineScope {
        return@coroutineScope try {
            val response = client.get(
                url = ficbookUrl {
                    href(section.path)
                    section.queryParameters?.let { queryParams(it) }
                    page(page)
                }
            )
            val body: String = response.body.stringOrThrow()
            val document = Jsoup.parse(body)
            val elements = fanficsListParser.parse(document)
            val pageButtons = checkPageButtonsExists(document)

            val fanfics = elements.map { fanficCardParser.parse(it) }

            println(pageButtons)
            ApiResult.success(
                ListResult(
                    list = fanfics,
                    hasNextPage = pageButtons.hasNext
                )
            )
        } catch (e: Exception) {
            ApiResult.Error(e)
        }
    }

    override suspend fun get(href: String, page: Int): ApiResult<ListResult<FanficCardModel>> {
        return get(SectionWithQuery(href), page)
    }
}