package ru.blays.ficbook.api.parsers

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.jsoup.nodes.Document
import org.jsoup.select.Evaluator
import ru.blays.ficbook.api.ATTR_HREF
import ru.blays.ficbook.api.ATTR_NAME
import ru.blays.ficbook.api.ATTR_SRC
import ru.blays.ficbook.api.AUTHORS_HREF
import ru.blays.ficbook.api.dataModels.PopularAuthorModel
import ru.blays.ficbook.api.dataModels.UserModel

internal class UserParser: IDataParser<Document, UserModel> {
    override suspend fun parse(data: Document): UserModel = coroutineScope {
        val profileHolder = data.select(
            Evaluator.Class("dropdown profile-holder")
        )
        val name = profileHolder.select("span[class=\"text hidden-xs\"]").text()

        val avatarUrl = profileHolder
            .select(".avatar-cropper")
            .select("img")
            .attr(ATTR_SRC)

        val href = profileHolder.select("li a").run {
            forEach { a ->
                val href = a.attr(ATTR_HREF)
                if(href.contains(AUTHORS_HREF)) {
                    return@run href
                }
            }
            return@run ""
        }

        return@coroutineScope UserModel(
            name= name,
            avatarUrl = avatarUrl,
            href = href
        )
    }

    override fun parseSynchronously(data: Document): StateFlow<UserModel?> {
        TODO("Not yet implemented")
    }
}

internal class FavouriteAuthorsParser: IDataParser<Document, List<UserModel>> {
    override suspend fun parse(data: Document): List<UserModel> {
        val userElements = data.select("div.data-table div[class=\"data-table-row d-flex justify-content-between\"]")
        val users = userElements.map { element ->
            val (name, href) = element.select("a").let {
                it.text() to it.attr(ATTR_HREF)
            }
            UserModel(
                name = name,
                href = href
            )
        }
        return users
    }
    override fun parseSynchronously(data: Document): StateFlow<List<UserModel>?> {
        TODO("Not yet implemented")
    }
}

internal class PopularAuthorsParser: IDataParser<Document, List<PopularAuthorModel>> {
    override suspend fun parse(data: Document): List<PopularAuthorModel> {
        val topAuthors = data.select(".top-authors")
        val tr = topAuthors.select("tbody tr")
        val authors = tr.map { element ->
            val avatarUrl = element.select("div.avatar-cropper img").attr(ATTR_SRC)
            val user = element.select("td a").let {
                UserModel(
                    name = it.text(),
                    href = it.attr(ATTR_HREF),
                    avatarUrl = avatarUrl
                )
            }
            val position = element.select("td a").attr(ATTR_NAME).toIntOrNull() ?: 0
            val subscribersInfo = element.select(".rating_descr").text().trim()
            PopularAuthorModel(
                user = user,
                position = position,
                subscribersInfo = subscribersInfo

            )
        }
        return authors
    }

    override fun parseSynchronously(data: Document): StateFlow<List<PopularAuthorModel>?> {
        TODO("Not yet implemented")
    }
}

