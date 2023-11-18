package ru.blays.ficbookapi.ficbookConnection

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.CookieJar
import okhttp3.internal.closeQuietly
import okio.use
import org.jsoup.Jsoup
import ru.blays.ficbookapi.*
import ru.blays.ficbookapi.data.SectionWithQuery
import ru.blays.ficbookapi.dataModels.*
import ru.blays.ficbookapi.parsers.*
import ru.blays.ficbookapi.result.ApiResult

open class FicbookApi: IFicbookApi {
    private var cookieJar: CookieJar? = null
    private val _currentUser: MutableStateFlow<UserModel?> = MutableStateFlow(null)
    private val _isAuthorized: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override val currentUser: StateFlow<UserModel?>
        get() = _currentUser

    override val isAuthorized: StateFlow<Boolean>
        get() = _isAuthorized

    override fun init(block: suspend IFicbookApi.() -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            block()
        }
    }

    override suspend fun setCookie(cookies: List<CookieModel>) = coroutineScope {
        this@FicbookApi.cookieJar = CustomCookieJar(cookies)
        _isAuthorized.value = checkAuthorization()
    }

    override fun logOut() {
        _isAuthorized.value = false
        _currentUser.value = null
        cookieJar = null
    }

    override suspend fun authorize(loginModel: LoginModel): AuthorizationResult = coroutineScope {
        val loginDataBody = loginModel.toRequestBody()
        val url = buildFicbookURL {
            addPathSegment("login_check")
        }
        val request = buildFicbookRequest {
            url(url)
            post(loginDataBody)
            header("authority", "ficbook.net")
            header("origin", "https://ficbook.net")
            header("referer", "https://ficbook.net/")
        }
        val response = makeRequest(
            request = request,
            cookieJar = cookieJar
        )
        val bodyString = response?.body?.string()

        val resultModel = if(bodyString != null) {
            Json.decodeFromString<AuthorizationResponseModel>(bodyString)
        } else {
            AuthorizationResponseModel(
                error = AuthorizationResponseModel.Error(
                    "Can't able to send request"
                ),
                success = false
            )
        }
        val cookiesHeaders = response
            ?.headers
            ?.values(HEADER_COOKIE_SET)

        val cookieParser = CookieParser()
        val cookies = cookiesHeaders?.let {
            cookieParser.parse(it)
        } ?: emptyList()

        response?.closeQuietly()

        return@coroutineScope AuthorizationResult(
            responseResult = resultModel,
            cookies = cookies
        )
    }

    override suspend fun checkAuthorization(): Boolean = coroutineScope {
        val url = buildFicbookURL {
            href(SETTING_HREF)
        }
        val request = buildFicbookRequest {
            url(url)
        }
        val response = makeRequest(
            request = request,
            cookieJar = cookieJar
        ) {
            followRedirects(false)
        }

        response?.use { resp ->
            val code = resp.code
            val body = resp.body?.string()
            val document = body?.let { Jsoup.parse(it) }

            val userParser = UserParser()

            val userModel = document?.let { userParser.parse(it) }
            _currentUser.value = userModel

            return@coroutineScope code == 200
        }
        return@coroutineScope false
    }

    override suspend fun getFanficsForHref(href: String, page: Int): ApiResult<FanficsListResult> {
        val section = SectionWithQuery(href = href)
        return getFanficsForSection(section, page)
    }

    override suspend fun getFanficsForSection(
        section: SectionWithQuery,
        page: Int
    ): ApiResult<FanficsListResult> = coroutineScope {
        val url = buildFicbookURL {
            href(section.path)
            section.queryParameters.forEach { (name, value) ->
                addQueryParameter(name, value)
            }
            page(page)
        }
        val request = buildFicbookRequest {
            url(url)
        }

        val body = getHtmlBody(
            request = request,
            cookieJar = cookieJar
        ) {
            followRedirects(false)
        }

        return@coroutineScope if(body.value != null) {
            val list = mutableListOf<FanficCardModel>()
            val fanficsListParser = FanficsListParser()
            val fanficCardParser = FanficCardParser()

            val (fanficsHtml, hasNextPage) = fanficsListParser.parse(body.value)
            fanficsHtml.forEach {
                list += fanficCardParser.parse(it)
            }
            ApiResult.Success(
                FanficsListResult(
                    fanfics = list,
                    hasNextPage = hasNextPage
                )
            )
        } else {
            ApiResult.Error("Unable to load fanfics list")
        }
    }

    override suspend fun getFanficPageByID(id: String): ApiResult<FanficPageModel> {
        return getFanficPageByHref(getFanficHrefForID(id))
    }

    private fun getFanficHrefForID(id: String): String {
        return "$READFIC_HREF/$id"
    }

    override suspend fun getFanficPageByHref(href: String): ApiResult<FanficPageModel> = coroutineScope {
        val url = buildFicbookURL {
            href(href)
        }
        val request = buildFicbookRequest {
            url(url)
        }

        val fanficPageParser = FanficPageParser()
        val bodyValue = getHtmlBody(
            request = request,
            cookieJar = cookieJar
        ).value
        return@coroutineScope if (bodyValue != null) {
            val page = fanficPageParser.parse(bodyValue)
            ApiResult.Success(page)
        } else {
            ApiResult.Error("Unable to load fanfic page")
        }
    }

    override suspend fun getFanficChapterText(href: String): ApiResult<String> = coroutineScope {
        val hrefWithoutFragment = href.removeSuffix(SUFFIX_PART_CONTENT)
        val url = buildFicbookURL {
            href(hrefWithoutFragment)
        }
        val request = buildFicbookRequest {
            url(url)
        }
        val bodyValue = getHtmlBody(
            request = request,
            cookieJar = cookieJar
        ).value

        return@coroutineScope if(bodyValue != null) {
            val chapterTextParser = SeparateChapterParser()
            val document = Jsoup.parse(bodyValue)
            val chapterText = chapterTextParser.parse(document)
            ApiResult.Success(chapterText)
        } else {
            ApiResult.Error("Unable to load chapter text")
        }
    }

    override suspend fun getCollections(section: SectionWithQuery): ApiResult<List<CollectionModel>> = coroutineScope {
        val url = buildFicbookURL {
            href(section.path)
            section.queryParameters.forEach { (name, value) ->
                addQueryParameter(name, value)
            }
        }
        val request = buildFicbookRequest {
            url(url)
        }
        val bodyValue = getHtmlBody(
            request = request,
            cookieJar = cookieJar
        ) {
            followRedirects(false)
        }.value

        return@coroutineScope if(bodyValue != null) {
            val document = Jsoup.parse(bodyValue)
            val collectionListParser = CollectionListParser()
            val collections = collectionListParser.parse(document)
            ApiResult.Success(collections)
        } else {
            ApiResult.Error("Unable to load collection list")
        }
    }

    override suspend fun getAuthorProfileForHref(href: String): ApiResult<AuthorProfileModel> = coroutineScope {
        val baseUrl = buildFicbookURL {
            href(href)
        }
        val infoPageRequest = buildFicbookRequest {
            url(baseUrl)
        }
        val infoPageResponse = makeRequest(
            request = infoPageRequest,
            cookieJar = cookieJar
        )
        if(infoPageResponse != null && infoPageResponse.code == 200) {
            val body = infoPageResponse.body
                ?.string()
                ?: return@coroutineScope ApiResult.Error("Unable to load author profile")

            val document = Jsoup.parse(body)
            val authorMain = AuthorMainInfoParser().parse(document)
            val availableTabs = authorMain.availableTabs

            val authorInfo = AuthorInfoParser().parse(document)

            val authorBlog = "${href.trim('/')}/${AuthorProfileTabs.BLOG.href}"

            val authorWorks = if(AuthorProfileTabs.WORKS in availableTabs) {
                val worksHref = "${href.trim('/')}/${AuthorProfileTabs.WORKS.href}"
                SectionWithQuery(
                    name = "Работы автора: ${authorMain.name}",
                    href = worksHref
                )
            } else {
                null
            }

            val authorWorksAsCoauthor = if(AuthorProfileTabs.WORKS_COAUTHOR in availableTabs) {
                val worksHref = "${href.trim('/')}/${AuthorProfileTabs.WORKS_COAUTHOR.href}"
                SectionWithQuery(
                    name = "Работы автора: ${authorMain.name}",
                    href = worksHref
                )
            } else {
                null
            }

            val authorWorksAsBeta = if(AuthorProfileTabs.WORKS_BETA in availableTabs) {
                val worksHref = "${href.trim('/')}/${AuthorProfileTabs.WORKS_BETA.href}"
                SectionWithQuery(
                    name = "Работы автора ${authorMain.name} как Бета",
                    href = worksHref
                )
            } else {
                null
            }

            val authorWorksAsGamma = if(AuthorProfileTabs.WORKS_GAMMA in availableTabs) {
                val worksHref = "${href.trim('/')}/${AuthorProfileTabs.WORKS_GAMMA.href}"
                SectionWithQuery(
                    name = "Работы автора ${authorMain.name} как Гамма",
                    href = worksHref
                )
            } else {
                null
            }

            val authorPresent = "${href.trim('/')}/${AuthorProfileTabs.PRESENTS.href}"

            val authorComments = "${href.trim('/')}/${AuthorProfileTabs.COMMENTS.href}"

            val authorProfileModel = AuthorProfileModel(
                authorMain = authorMain,
                authorInfo = authorInfo,
                authorBlogHref = authorBlog,
                authorWorks = authorWorks,
                authorWorksAsCoauthor = authorWorksAsCoauthor,
                authorWorksAsBeta = authorWorksAsBeta,
                authorWorksAsGamma = authorWorksAsGamma,
                authorPresentsHref = authorPresent,
                authorCommentsHref = authorComments
            )

            return@coroutineScope ApiResult.Success(
                value = authorProfileModel
            )
        }
        return@coroutineScope ApiResult.Error("Unable to load author profile")
    }

    override suspend fun getAuthorProfileByID(id: String): ApiResult<AuthorProfileModel> = getAuthorProfileForHref(
        href = "/$AUTHOR_PROFILE/$id"
    )

    override suspend fun getAuthorBlogPosts(
        href: String,
        page: Int
    ): ApiResult<List<BlogPostCardModel>> = coroutineScope {
        val url = buildFicbookURL {
            href(href)
            addQueryParameter(QUERY_PAGE, page.toString())
            page(page)
        }
        val request = buildFicbookRequest {
            url(url)
        }
        val body = getHtmlBody(
            request = request,
            cookieJar = cookieJar
        ).value

        return@coroutineScope if(body != null) {
            val document = Jsoup.parse(body)
            ApiResult.Success(
                value = AuthorBlogPostsParser().parse(document)
            )
        } else {
            ApiResult.Error("Unable to load posts")
        }
    }

    override suspend fun getAuthorPresents(
        href: String,
        page: Int
    ): ApiResult<List<AuthorPresentModel>> = coroutineScope {
        val url = buildFicbookURL {
            href(href)
            page(page)
        }
        val request = buildFicbookRequest {
            url(url)
        }
        val body = getHtmlBody(
            request = request,
            cookieJar = cookieJar
        ).value

        return@coroutineScope if (body != null) {
            val document = Jsoup.parse(body)
            ApiResult.Success(
                value = AuthorPresentsParser().parse(document)
            )
        } else {
            ApiResult.Error("Unable to load presents")
        }
    }

    override suspend fun getAuthorFanficsPresents(
        href: String,
        page: Int
    ): ApiResult<List<AuthorFanficPresentModel>> = coroutineScope {
        val url = buildFicbookURL {
            href(href)
            page(page)
        }
        val request = buildFicbookRequest {
            url(url)
        }
        val body = getHtmlBody(
            request = request,
            cookieJar = cookieJar
        ).value

        return@coroutineScope if (body != null) {
            val document = Jsoup.parse(body)
            ApiResult.Success(
                value = AuthorFanficPresentsParser().parse(document)
            )
        } else {
            ApiResult.Error("Unable to load presents")
        }
    }

    override suspend fun getAuthorCommentsPresents(
        href: String,
        page: Int
    ): ApiResult<List<AuthorCommentPresentModel>> = coroutineScope {
        val url = buildFicbookURL {
            href(href)
            page(page)
        }
        val request = buildFicbookRequest {
            url(url)
        }
        val body = getHtmlBody(
            request = request,
            cookieJar = cookieJar
        ).value

        return@coroutineScope if (body != null) {
            val document = Jsoup.parse(body)
            ApiResult.Success(
                value = AuthorCommentPresentsParser().parse(document)
            )
        } else {
            ApiResult.Error("Unable to load presents")
        }
    }

    override suspend fun getAuthorBlogPost(href: String): ApiResult<BlogPostPageModel> = coroutineScope {
        val url = buildFicbookURL {
            href(href)
        }
        val request = buildFicbookRequest {
            url(url)
        }
        val body = getHtmlBody(
            request = request,
            cookieJar = cookieJar
        ).value
        return@coroutineScope if (body != null) {
            val document = Jsoup.parse(body)
            ApiResult.Success(
                value = AuthorBlogPostParser().parse(document)
            )
        } else {
            ApiResult.Error("Unable to load post")
        }
    }

    override suspend fun getFandomsForSection(section: String, page: Int): List<FandomModel> {
        val url = buildFicbookURL {
            addPathSegments(section)
            page(page)
        }
        val request = buildFicbookRequest {
            url(url)
        }

        val fandomParser = FandomParser()

        val body = getHtmlBody(
            request = request,
            cookieJar = cookieJar
        )
        val document = body.value?.let { Jsoup.parse(it) }
        val fandoms = document?.let { fandomParser.parse(it) }
        return fandoms ?: emptyList()
    }

    override suspend fun actionChangeFollow(follow: Boolean, fanficID: String): Boolean {
        return ru.blays.ficbookapi.ajax.actionChangeFollow(
            follow = follow,
            cookieJar = cookieJar,
            fanficID = fanficID
        )
    }

    override suspend fun actionChangeMark(mark: Boolean, fanficID: String): Boolean {
        return ru.blays.ficbookapi.ajax.actionChangeMark(
            mark = mark,
            cookieJar = cookieJar,
            fanficID = fanficID

        )
    }

    override suspend fun actionChangeRead(read: Boolean, fanficID: String): Boolean {
        return ru.blays.ficbookapi.ajax.actionChangeRead(
            read = read,
            cookieJar = cookieJar,
            fanficID = fanficID
        )
    }

    override suspend fun actionChangeVote(vote: Boolean, chapterHref: String): Boolean {
        return ru.blays.ficbookapi.ajax.actionChangeVote(
            vote = vote,
            cookieJar = cookieJar,
            chapterHref = chapterHref
        )
    }
}