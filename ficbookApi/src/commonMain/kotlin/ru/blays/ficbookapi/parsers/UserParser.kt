package ru.blays.ficbookapi.parsers

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.jsoup.nodes.Document
import org.jsoup.select.Evaluator
import ru.blays.ficbookapi.ATTR_HREF
import ru.blays.ficbookapi.ATTR_SRC
import ru.blays.ficbookapi.AUTHOR_PROFILE
import ru.blays.ficbookapi.dataModels.UserModel

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
                if(href.contains(AUTHOR_PROFILE)) {
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