package ru.blays.ficbookapi.api

import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import ru.blays.ficbookapi.AUTHORS_HREF
import ru.blays.ficbookapi.AUTHOR_SEARCH_HREF
import ru.blays.ficbookapi.FAVOURITE_AUTHORS_HREF
import ru.blays.ficbookapi.dataModels.AuthorSearchResult
import ru.blays.ficbookapi.dataModels.ListResult
import ru.blays.ficbookapi.dataModels.PopularAuthorModel
import ru.blays.ficbookapi.dataModels.UserModel
import ru.blays.ficbookapi.ficbookExtensions.ficbookUrl
import ru.blays.ficbookapi.json
import ru.blays.ficbookapi.okHttpDsl.*
import ru.blays.ficbookapi.parsers.FavouriteAuthorsParser
import ru.blays.ficbookapi.parsers.PopularAuthorsParser
import ru.blays.ficbookapi.parsers.checkPageButtonsExists
import ru.blays.ficbookapi.result.ApiResult

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
