package ru.blays.ficbook.api.api

import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ru.blays.ficbook.api.ADD_AUTHOR_TO_FAVOURITE_HREF
import ru.blays.ficbook.api.QUERY_TAB
import ru.blays.ficbook.api.REMOVE_AUTHOR_FROM_FAVOURITE_HREF
import ru.blays.ficbook.api.dataModels.*
import ru.blays.ficbook.api.ficbookExtensions.ficbookUrl
import ru.blays.ficbook.api.json
import ru.blays.ficbook.api.okHttpDsl.*
import ru.blays.ficbook.api.parsers.*
import ru.blays.ficbook.api.result.ApiResult
import kotlin.reflect.KSuspendFunction1

interface AuthorProfileApi {
    suspend fun getByHref(href: String): ApiResult<AuthorProfileModel>
    suspend fun getByID(id: String): ApiResult<AuthorProfileModel>

    suspend fun changeFollow(follow: Boolean, id: String): ApiResult<Boolean>

    suspend fun getBlogPosts(id: String, page: Int): ApiResult<ListResult<BlogPostCardModel>>
    suspend fun getBlogPage(userId: String, blogId: String): ApiResult<BlogPostPageModel>

    suspend fun getAuthorPresents(id: String, page: Int): ApiResult<ListResult<AuthorPresentModel>>
    suspend fun getFanficsPresents(id: String, page: Int): ApiResult<ListResult<AuthorFanficPresentModel>>
    suspend fun getCommentsPresents(id: String, page: Int): ApiResult<ListResult<AuthorCommentPresentModel>>
}

internal class AuthorProfileApiImpl(
    private val client: OkHttpClient
): AuthorProfileApi {
    private val authorProfileParser = AuthorMainInfoParser()
    private val authorInfoParser = AuthorInfoParser()
    private val availableTabsParser = AuthorProfileTabsParser()
    private val authorBlogPostsParser = AuthorBlogPostsParser()
    private val authorBlogPostParser = AuthorBlogPostParser()
    private val authorPresentsParser =  AuthorPresentsParser()
    private val authorFanficPresentsParser = AuthorFanficPresentsParser()
    private val authorCommentsPresentsParser = AuthorCommentPresentsParser()

    override suspend fun getByHref(href: String): ApiResult<AuthorProfileModel> = coroutineScope {
        return@coroutineScope try {
            val response = client.get {
                ficbookUrl {
                    href(href)
                }
            }
            val body = response.body.stringOrThrow()
            val document = Jsoup.parse(body)
            val mainInfo = authorProfileParser.parse(document)
            val authorInfo = authorInfoParser.parse(document)
            val tabs = availableTabsParser.parse(document)
            ApiResult.success(
                AuthorProfileModel(
                    authorMain = mainInfo,
                    authorInfo = authorInfo,
                    availableTabs = tabs
                )
            )
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }

    override suspend fun getByID(id: String): ApiResult<AuthorProfileModel> {
        return getByHref("authors/$id")
    }

    override suspend fun changeFollow(follow: Boolean, id: String): ApiResult<Boolean> = coroutineScope {
        return@coroutineScope try {
            val response = client.post(
                url = ficbookUrl {
                    if(follow) {
                        href(ADD_AUTHOR_TO_FAVOURITE_HREF)
                    } else {
                        href(REMOVE_AUTHOR_FROM_FAVOURITE_HREF)
                    }
                },
                body = formBody {
                    add("author_id", id)
                }
            )
            val body: String = response.body.stringOrThrow()
            val result: AjaxSimpleResult = json.decodeFromString(body)
            ApiResult.success(result.result)
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }

    override suspend fun getBlogPosts(id: String, page: Int): ApiResult<ListResult<BlogPostCardModel>> = coroutineScope {
        return@coroutineScope try{
            val response = client.get {
                ficbookUrl {
                    addPathSegment("authors")
                    addPathSegment(id)
                    addPathSegment("blog")
                    page(page)
                }
            }
            val body: String = response.body.stringOrThrow()
            val document = Jsoup.parse(body)
            val posts = authorBlogPostsParser.parse(document)
            val pageButtons = checkPageButtonsExists(document)
            ApiResult.success(
                ListResult(
                    list = posts,
                    hasNextPage = pageButtons.hasNext
                )
            )
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }

    override suspend fun getBlogPage(userId: String, blogId: String): ApiResult<BlogPostPageModel> = coroutineScope {
        return@coroutineScope try{
            val response = client.get {
                ficbookUrl {
                    addPathSegment("authors")
                    addPathSegment(userId)
                    addPathSegment("blog")
                    addPathSegment(blogId)
                }
            }
            val body: String = response.body.stringOrThrow()
            val document = Jsoup.parse(body)
            val post = authorBlogPostParser.parse(document)
            ApiResult.success(post)
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }

    override suspend fun getAuthorPresents(id: String, page: Int): ApiResult<ListResult<AuthorPresentModel>> {
        return getPresents(
            id = id,
            tab = 1,
            page = page,
            parser = authorPresentsParser::parse
        )
    }

    override suspend fun getFanficsPresents(id: String, page: Int): ApiResult<ListResult<AuthorFanficPresentModel>> {
        return getPresents(
            id = id,
            tab = 2,
            page = page,
            parser = authorFanficPresentsParser::parse
        )
    }

    override suspend fun getCommentsPresents(id: String, page: Int): ApiResult<ListResult<AuthorCommentPresentModel>> {
        return getPresents(
            id = id,
            tab = 3,
            page = page,
            parser = authorCommentsPresentsParser::parse
        )
    }

    private suspend inline fun <reified T: Any> getPresents(
        id: String,
        tab: Int,
        page: Int,
        crossinline parser: KSuspendFunction1<Document, List<T>>
    ): ApiResult<ListResult<T>> = coroutineScope {
        return@coroutineScope try {
            val response = client.get {
                ficbookUrl {
                    addPathSegment("authors")
                    addPathSegment(id)
                    addPathSegment("presents")
                    addQueryParameter(QUERY_TAB, tab.toString())
                    page(page)
                }
            }
            val body: String = response.body.stringOrThrow()
            val document = Jsoup.parse(body)
            val presents = parser(document)
            val pageButtons = checkPageButtonsExists(document)
            ApiResult.success(
                ListResult(
                    list = presents,
                    hasNextPage = pageButtons.hasNext
                )
            )
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }
}