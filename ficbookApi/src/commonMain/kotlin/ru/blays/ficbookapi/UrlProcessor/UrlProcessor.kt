package ru.blays.ficbookapi.UrlProcessor

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import ru.blays.ficbookapi.FICBOOK_HOST
import ru.blays.ficbookapi.data.SectionWithQuery
import ru.blays.ficbookapi.data.UserSections

object UrlProcessor {

    fun analyzeUrl(url: String): FicbookUrlAnalyzeResult {
        val httpUrl = url.toHttpUrlOrNull()
            ?: return FicbookUrlAnalyzeResult.NotAUrl

        if(httpUrl.host == FICBOOK_HOST) {
            val pathSegments = httpUrl.pathSegments
            if(pathSegments.size > 1) {
                fanficsListChecker(pathSegments)?.let {
                    return it
                }
                userChecker(pathSegments)?.let {
                    return it
                }
                fanficPageChecker(pathSegments)?.let {
                    return it
                }
                notificationChecker(pathSegments)?.let {
                    return it
                }
            }
        }
        return FicbookUrlAnalyzeResult.NotFicbookUrl
    }

    private fun fanficsListChecker(pathSegments: List<String>): FicbookUrlAnalyzeResult.FanficsList? {
        if(pathSegments[0] == FANFICTION && pathSegments.size == 1) {
            return FicbookUrlAnalyzeResult.FanficsList(
                href = pathSegments[1]
            )
        }

        if(pathSegments[0] == FANFICTION && pathSegments.size >= 3) {
            return FicbookUrlAnalyzeResult.FanficsList(
                sectionWithQuery = SectionWithQuery(
                    paths = pathSegments.toTypedArray()
                )
            )
        }

        if(pathSegments[0] == POPULAR_SECTION) {
            return FicbookUrlAnalyzeResult.FanficsList(
                sectionWithQuery = SectionWithQuery(
                    paths = pathSegments.toTypedArray()
                )
            )
        }

        if(pathSegments[0] == FIND) {
            return FicbookUrlAnalyzeResult.FanficsList(
                sectionWithQuery = SectionWithQuery(
                    paths = pathSegments.toTypedArray()
                )
            )
        }

        if(pathSegments[0] == COLLECTIONS) {
            return FicbookUrlAnalyzeResult.FanficsList(
                sectionWithQuery = SectionWithQuery(
                    paths = pathSegments.toTypedArray()
                )
            )
        }

        if(
            pathSegments[0] == HOME && UserSections
                .default()
                .all
                .any { it.path.contains(pathSegments[1]) }
        ) {
            return FicbookUrlAnalyzeResult.FanficsList(
                sectionWithQuery = SectionWithQuery(
                    paths = pathSegments.toTypedArray()
                )
            )
        }

        return null
    }

    private fun userChecker(pathSegments: List<String>): FicbookUrlAnalyzeResult.User? {
        if (pathSegments[0] == AUTHORS) {
            return FicbookUrlAnalyzeResult.User(
                href = pathSegments.joinToString("/")
            )
        }
        return null
    }

    private fun fanficPageChecker(pathSegments: List<String>): FicbookUrlAnalyzeResult.Fanfic? {
        return if(pathSegments[0] == READFIC) {
            val href = "${pathSegments[0]}/${pathSegments.getOrElse(1) {""}}"
            FicbookUrlAnalyzeResult.Fanfic(href = href)
        } else null
    }

    private fun notificationChecker(pathSegments: List<String>): FicbookUrlAnalyzeResult.Notifications? {
        return if(pathSegments[0] == NOTIFICATION) {
            FicbookUrlAnalyzeResult.Notifications
        } else null
    }

    private const val FANFICTION = "fanfiction"
    private const val AUTHORS = "authors"
    private const val HOME = "home"
    private const val POPULAR_SECTION = "popular-fanfics-376846"
    private const val FIND = "find-fanfics-846555"
    private const val COLLECTIONS = "collections"
    private const val READFIC = "readfic"
    private const val NOTIFICATION = "notifications"

    sealed class FicbookUrlAnalyzeResult {
        data class FanficsList(val sectionWithQuery: SectionWithQuery) : FicbookUrlAnalyzeResult() {
            constructor(
                href: String
            ): this(
                sectionWithQuery = SectionWithQuery(
                    href = href
                )
            )
        }
        data class Fanfic(val href: String) : FicbookUrlAnalyzeResult()
        data class User(val href: String) : FicbookUrlAnalyzeResult()
        data object Notifications: FicbookUrlAnalyzeResult()
        data object NotFicbookUrl : FicbookUrlAnalyzeResult()
        data object NotAUrl : FicbookUrlAnalyzeResult()
    }
}