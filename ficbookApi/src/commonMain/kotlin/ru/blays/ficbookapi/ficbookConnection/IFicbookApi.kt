package ru.blays.ficbookapi.ficbookConnection

import kotlinx.coroutines.flow.StateFlow
import ru.blays.ficbookapi.data.SectionWithQuery
import ru.blays.ficbookapi.dataModels.*
import ru.blays.ficbookapi.result.ApiResult

interface IFicbookApi {
    val isAuthorized: StateFlow<Boolean>
    val currentUser: StateFlow<UserModel?>

    fun init(block: suspend IFicbookApi.() -> Unit)

    suspend fun authorize(loginModel: LoginModel): AuthorizationResult
    suspend fun checkAuthorization(): Boolean
    suspend fun setCookie(cookies: List<CookieModel>)
    fun logOut()

    suspend fun getFanficPageByID(id: String): ApiResult<FanficPageModel>
    suspend fun getFanficPageByHref(href: String): ApiResult<FanficPageModel>

    suspend fun getFanficsForHref(href: String, page: Int): ApiResult<List<FanficCardModel>>
    suspend fun getFanficsForSection(section: SectionWithQuery, page: Int): ApiResult<List<FanficCardModel>>

    suspend fun getFandomsForSection(section: String, page: Int): List<FandomModel>

    suspend fun getFanficChapterText(href: String): ApiResult<String>

    suspend fun getCollections(section: SectionWithQuery): ApiResult<List<CollectionModel>>

    suspend fun actionChangeFollow(
        follow: Boolean,
        fanficID: String
    ): Boolean
    suspend fun actionChangeMark(
       mark: Boolean,
       fanficID: String
    ): Boolean
    suspend fun actionChangeRead(
        read: Boolean,
        fanficID: String
    ): Boolean
    suspend fun actionChangeVote(
        vote: Boolean,
        chapterHref: String
    ): Boolean
}