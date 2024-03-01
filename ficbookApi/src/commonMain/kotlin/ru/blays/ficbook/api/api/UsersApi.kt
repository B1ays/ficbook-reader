package ru.blays.ficbook.api.api

import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import ru.blays.ficbook.api.AUTHORS_HREF
import ru.blays.ficbook.api.AUTHOR_SEARCH_HREF
import ru.blays.ficbook.api.FAVOURITE_AUTHORS_HREF
import ru.blays.ficbook.api.dataModels.AuthorSearchResult
import ru.blays.ficbook.api.dataModels.ListResult
import ru.blays.ficbook.api.dataModels.PopularAuthorModel
import ru.blays.ficbook.api.dataModels.UserModel
import ru.blays.ficbook.api.ficbookExtensions.ficbookUrl
import ru.blays.ficbook.api.json
import ru.blays.ficbook.api.okHttpDsl.*
import ru.blays.ficbook.api.parsers.FavouriteAuthorsParser
import ru.blays.ficbook.api.parsers.PopularAuthorsParser
import ru.blays.ficbook.api.parsers.checkPageButtonsExists
import ru.blays.ficbook.api.result.ApiResult
interface UsersApi {
    suspend fun getFavouritesAuthors(page: Int): ApiResult<ListResult<UserModel>>

    suspend fun getPopularAuthors(): ApiResult<List<PopularAuthorModel>>

    suspend fun searchAuthor(name: String, page: Int): ApiResult<AuthorSearchResult>
}

internal class UsersApiImpl(
    private val client: OkHttpClient
) : UsersApi {
    private val favouriteAuthorsParser = FavouriteAuthorsParser()
    private val popularAuthorsParser = PopularAuthorsParser()

    override suspend fun getFavouritesAuthors(
        page: Int
    ): ApiResult<ListResult<UserModel>> = coroutineScope {
        return@coroutineScope try {
            val response = client.get(
                url = ficbookUrl {
                    href(FAVOURITE_AUTHORS_HREF)
                    page(page)
                }
            )
            val body = response.body.stringOrThrow()
            val document = Jsoup.parse(body)
            val users = favouriteAuthorsParser.parse(document)
            val pageButtons = checkPageButtonsExists(document)
            ApiResult.success(
                ListResult(
                    list = users,
                    hasNextPage = pageButtons.hasNext
                )
            )
        } catch (e: Exception) {
            ApiResult.Error(e)
        }
    }

    override suspend fun getPopularAuthors(): ApiResult<List<PopularAuthorModel>> = coroutineScope {
        return@coroutineScope try {
            val response = client.get(
                url = ficbookUrl {
                    href(AUTHORS_HREF)
                }
            )
            val body = response.body.stringOrThrow()
            val document = Jsoup.parse(body)
            val users = popularAuthorsParser.parse(document)
            ApiResult.success(users)
        } catch (e: Exception) {
            ApiResult.Error(e)
        }
    }

    override suspend fun searchAuthor(
        name: String,
        page: Int
    ): ApiResult<AuthorSearchResult> = coroutineScope {
        return@coroutineScope try {
            val response = client.post(
                url = ficbookUrl {
                    href(AUTHOR_SEARCH_HREF)
                },
                body = formBody {
                    add("q", name)
                    add("page", page.toString())
                }
            )
            val body = response.body.stringOrThrow()
            val result: AuthorSearchResult = json.decodeFromString(body)
            ApiResult.success(result)
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }
}
