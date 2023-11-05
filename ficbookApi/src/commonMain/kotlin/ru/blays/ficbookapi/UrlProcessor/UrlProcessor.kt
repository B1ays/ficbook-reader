package ru.blays.ficbookapi.UrlProcessor

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import ru.blays.ficbookapi.FICBOOK_HOST
import ru.blays.ficbookapi.data.SectionWithQuery
import ru.blays.ficbookapi.data.UserSections

object UrlProcessor {

    fun analyzeUrl(url: String): FicbookUrlAnalyzeResult {
        val httpUrl = url.toHttpUrlOrNull() ?: return FicbookUrlAnalyzeResult.NotALink

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
            }
        }
        return FicbookUrlAnalyzeResult.NotFicbookUrl
    }

    private fun fanficsListChecker(pathSegments: List<String>): FicbookUrlAnalyzeResult.FanficsList? {
        val fanfictionRootSection = pathSegments[0] == FANFICTION && pathSegments.size == 1
        val fanfictionChildSection = pathSegments[0] == FANFICTION && pathSegments.size >= 3

        val userSection = pathSegments[0] == HOME && UserSections
            .default()
            .all
            .any { it.path.contains(pathSegments[1]) }

        val popularSection = pathSegments[0] == POPULAR_SECTION
        val findSection = pathSegments[0] == FIND
        val collectionsSection = pathSegments[0] == COLLECTIONS


        return when {
            fanfictionRootSection -> FicbookUrlAnalyzeResult.FanficsList(href = pathSegments[1])
            fanfictionChildSection -> FicbookUrlAnalyzeResult.FanficsList(
                sectionWithQuery = SectionWithQuery(
                    paths = pathSegments.toTypedArray()
                )
            )
            userSection -> FicbookUrlAnalyzeResult.FanficsList(
                sectionWithQuery = SectionWithQuery(
                    paths = pathSegments.toTypedArray()
                )
            )
            popularSection -> FicbookUrlAnalyzeResult.FanficsList(
                sectionWithQuery = SectionWithQuery(
                    paths = pathSegments.toTypedArray()
                )
            )
            findSection -> FicbookUrlAnalyzeResult.FanficsList(
                sectionWithQuery = SectionWithQuery(
                    paths = pathSegments.toTypedArray()
                )
            )
            collectionsSection -> FicbookUrlAnalyzeResult.FanficsList(
                sectionWithQuery = SectionWithQuery(
                    paths = pathSegments.toTypedArray()
                )
            )
            else -> null
        }
    }

    private fun userChecker(pathSegments: List<String>): FicbookUrlAnalyzeResult.User? {
        if (pathSegments[0] == AUTHORS) {
            return FicbookUrlAnalyzeResult.User(id = pathSegments[1])
        }
        return null
    }

    private fun fanficPageChecker(pathSegments: List<String>): FicbookUrlAnalyzeResult.Fanfic? {
        return if(pathSegments[0] == READFIC) {
            FicbookUrlAnalyzeResult.Fanfic(
                href = pathSegments.joinToString("/")
            )
        } else null
    }

    private const val FANFICTION = "fanfiction"
    private const val AUTHORS = "authors"
    private const val HOME = "home"
    private const val POPULAR_SECTION = "popular-fanfics-376846"
    private const val FIND = "find-fanfics-846555"
    private const val COLLECTIONS = "collections"
    private const val READFIC = "readfic"

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
        data class User(val id: String) : FicbookUrlAnalyzeResult()

        data object NotFicbookUrl : FicbookUrlAnalyzeResult()
        data object NotALink : FicbookUrlAnalyzeResult()
    }
}