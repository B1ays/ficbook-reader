package ru.blays.ficbookapi.parsers

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.jsoup.nodes.Document
import org.jsoup.select.Evaluator
import ru.blays.ficbookapi.dataModels.UserModel

class UserParser: IDataParser<Document, UserModel> {
    override suspend fun parse(data: Document): UserModel = coroutineScope {
        val profileHolder = data.select(
            Evaluator.Class("dropdown profile-holder")
        )
        val name = profileHolder.select(
            Evaluator.Class("text hidden-xs").toString()
        ).text()

        val avatarUrl = profileHolder
            .select(".avatar-cropper")
            .select("img")
            .attr("src")

        return@coroutineScope UserModel(
            name= name,
            avatarUrl = avatarUrl
        )
    }

    override fun parseSynchronously(data: Document): StateFlow<UserModel?> {
        TODO("Not yet implemented")
    }
}