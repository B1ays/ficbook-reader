package ru.blays.ficbookapi.ficbookConnection

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.internal.closeQuietly
import okio.use
import org.jsoup.Jsoup
import ru.blays.ficbookapi.*
import ru.blays.ficbookapi.data.SectionWithQuery
import ru.blays.ficbookapi.dataModels.*
import ru.blays.ficbookapi.parsers.*

open class FicbookApi: IFicbookApi {

    private var cookies: List<CookieModel> = emptyList()
    private val _user: MutableStateFlow<UserModel?> = MutableStateFlow(null)
    private val _isAuthorized: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override val currentUser: StateFlow<UserModel?>
        get() = _user

    override val isAuthorized: StateFlow<Boolean>
        get() = _isAuthorized


    override fun init(block: suspend IFicbookApi.() -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            block()
        }
    }

    override suspend fun setCookie(cookies: List<CookieModel>) = coroutineScope {
        this@FicbookApi.cookies = cookies
        _isAuthorized.value = checkAuthorization()
    }

    override fun logOut() {
        _isAuthorized.value = false
        _user.value = null
        cookies = emptyList()
    }

    override suspend fun authorize(loginModel: LoginModel): AuthorizationResult {
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
        val response = makeRequest(request)
        val bodyString = response?.body?.string()
        val resultModel = bodyString?.let {
            Json.decodeFromString<AuthorizationResponseModel>(it)
        }
        ?: AuthorizationResponseModel(
            error = AuthorizationResponseModel.Error(
                "Can't able to send request"
            ),
            success = false
        )
        val cookiesHeaders = response
            ?.headers
            ?.values(HEADER_COOKIE_SET)

        val cookieParser = CookieParser()
        val cookies = cookiesHeaders?.let {
            cookieParser.parse(it)
        }
        ?: emptyList()

        response?.closeQuietly()

        return AuthorizationResult(
            resultModel,
            cookies
        )
    }

    override suspend fun checkAuthorization(): Boolean = coroutineScope {
        val url = buildFicbookURL {
            href(SETTING_HREF)
        }
        val request = buildFicbookRequest(cookies) {
            url(url)
        }
        val response = makeRequest(request) {
            followRedirects(false)
        }
        //println("Current cookies: ${cookies.joinToString { "${it.name} | ${it.value}" }}")
        response?.use { resp ->
            val code = resp.code
            val body = resp.body?.string()
            val document = body?.let { Jsoup.parse(it) }

            val userParser = UserParser()

            val userModel = document?.let { userParser.parse(it) }
            _user.value = userModel

            return@coroutineScope code == 200
        }
        return@coroutineScope false
    }

    override suspend fun getFanficsForHref(href: String, page: Int): List<FanficModel> {
        val section = SectionWithQuery(href = href)
        return getFanficsForSection(section, page)
    }

    override suspend fun getFanficsForSection(section: SectionWithQuery, page: Int): List<FanficModel> {
        val url = buildFicbookURL {
            href(section.path)
            section.queryParameters.forEach { (name, value) ->
                addQueryParameter(name, value)
            }
        }
        val request = buildFicbookRequest(cookies) {
            url(url)
        }
        val list = mutableListOf<FanficModel>()

        val body = getHtmlBody(request) {
            followRedirects(false)
        }
        val fanficsListParser = FanficsListParser()
        val fanficCardParser = FanficCardParser()

        val fanficsHtml = body?.let { fanficsListParser.parse(it) }
        fanficsHtml?.forEach {
            list += fanficCardParser.parse(it)
        }
        return list
    }

    override suspend fun getFanficPageByID(id: String): FanficPageModel? {
        return getFanficPageByHref(getFanficHrefForID(id))
    }

    private fun getFanficHrefForID(id: String): String {
        return "$READFIC_HREF/$id"
    }

    override suspend fun getFanficPageByHref(href: String): FanficPageModel? {

        val url = buildFicbookURL {
            href(href)
        }
        val request = buildFicbookRequest(cookies) {
            url(url)
        }

        val fanficPageParser = FanficPageParser()
        val body = getHtmlBody(request)
        return body?.let { fanficPageParser.parse(it) }
    }

    override suspend fun getFanficChapterText(href: String): String? = coroutineScope {
        val hrefWithoutFragment = href.removeSuffix(SUFFIX_PART_CONTENT)
        val url = buildFicbookURL {
            href(hrefWithoutFragment)
        }
        val request = buildFicbookRequest(cookies) {
            url(url)
        }
        val body = getHtmlBody(request)
        val chapterTextParser = SeparateChapterParser()
        val document = body?.let { Jsoup.parse(it) }

        return@coroutineScope if(document != null) {
            chapterTextParser.parse(document)
        } else null
    }

    override suspend fun getCollections(section: SectionWithQuery): List<CollectionModel> {
        val url = buildFicbookURL {
            href(section.path)
            section.queryParameters.forEach { (name, value) ->
                addQueryParameter(name, value)
            }
        }
        val request = buildFicbookRequest(cookies) {
            url(url)
        }
        val body = getHtmlBody(request) {
            followRedirects(false)
        }
        val document = body?.let { Jsoup.parse(it) }

        val collectionListParser = CollectionListParser()

        return if(document != null) {
            collectionListParser.parse(document)
        } else {
            emptyList()
        }
    }

    override suspend fun getFandomsForSection(section: String, page: Int): List<FandomModel> {
        val url = buildFicbookURL {
            addPathSegments(section)
            page(page)
        }
        val request = buildFicbookRequest(cookies) {
            url(url)
        }

        val fandomParser = FandomParser()

        val body = getHtmlBody(request)
        val document = body?.let { Jsoup.parse(it) }
        val fandoms = document?.let { fandomParser.parse(it) }
        return fandoms ?: emptyList()
    }

    override suspend fun actionChangeFollow(follow: Boolean, fanficID: String): Boolean {
        return ru.blays.ficbookapi.ajax.actionChangeFollow(
            follow = follow,
            cookies = cookies,
            fanficID = fanficID
        )
    }

    override suspend fun actionChangeMark(mark: Boolean, fanficID: String): Boolean {
        return ru.blays.ficbookapi.ajax.actionChangeMark(
            mark = mark,
            cookies = cookies,
            fanficID = fanficID

        )
    }

    override suspend fun actionChangeRead(read: Boolean, fanficID: String): Boolean {
        return ru.blays.ficbookapi.ajax.actionChangeRead(
            read, cookies, fanficID
        )
    }

    override suspend fun actionChangeVote(vote: Boolean, chapterHref: String): Boolean {
        return ru.blays.ficbookapi.ajax.actionChangeVote(
            vote, cookies, chapterHref
        )
    }
}