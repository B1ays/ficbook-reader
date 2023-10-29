package ru.blays.ficbookapi.ficbookConnection

import kotlinx.coroutines.flow.StateFlow
import ru.blays.ficbookapi.data.SectionWithQuery
import ru.blays.ficbookapi.dataModels.*

interface IFicbookApi {
    val isAuthorized: StateFlow<Boolean>
    val currentUser: StateFlow<UserModel?>

    fun init(block: suspend IFicbookApi.() -> Unit)

    suspend fun authorize(loginModel: LoginModel): AuthorizationResult
    suspend fun checkAuthorization(): Boolean
    suspend fun setCookie(cookies: List<CookieModel>)
    fun logOut()

    suspend fun getFanficPageByID(id: String): FanficPageModel?
    suspend fun getFanficPageByHref(href: String): FanficPageModel?

    suspend fun getFanficsForHref(href: String, page: Int): List<FanficModel>
    suspend fun getFanficsForSection(section: SectionWithQuery, page: Int): List<FanficModel>

    suspend fun getFandomsForSection(section: String, page: Int): List<FandomModel>

    suspend fun getFanficChapterText(href: String): String?

    suspend fun getCollections(section: SectionWithQuery): List<CollectionModel>

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