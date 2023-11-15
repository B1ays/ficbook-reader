package ru.blays.ficbookapi.parsers

import kotlinx.coroutines.flow.StateFlow
import org.jsoup.nodes.Document
import org.jsoup.select.Evaluator
import ru.blays.ficbookapi.data.Section
import ru.blays.ficbookapi.dataModels.*

internal class AuthorMainInfoParser : IDataParser<Document, AuthorMainInfo> {
    override suspend fun parse(data: Document): AuthorMainInfo {
        val profileHeader = data.select(".profile-header")

        val name = profileHeader
            .select(".user-name")
            .text()
            .trim()

        val id = data.select("[name=userId]").attr("value")

        val avatarUrl = profileHeader
            .select(".avatar-cropper")
            .select("img")
            .attr("src")

        val profileCoverUrl = profileHeader
            .select(".img-responsive")
            .attr("src")

        val subscribers = profileHeader
            .select("div.small-text a")
            .text()
            .replace(
                regex = Regex("[^0-9]+"),
                replacement = ""
            )
            .toIntOrNull()
            ?: 0

        val availableTabs = AuthorProfileTabsParser().parse(data)

        return AuthorMainInfo(
            name = name,
            id = id,
            avatarUrl = avatarUrl,
            profileCoverUrl = profileCoverUrl,
            subscribers = subscribers
        ).apply {
            this.availableTabs = availableTabs
        }
    }

    override fun parseSynchronously(data: Document): StateFlow<AuthorMainInfo?> {
        TODO("Not yet implemented")
    }
}

internal class AuthorProfileTabsParser: IDataParser<Document, List<AuthorProfileTabs>> {
    override suspend fun parse(data: Document): List<AuthorProfileTabs> {
        val sideBarNavListItems = data
            .select(".sidebar-nav-list")
            .select("li a")

        val tabs: MutableList<AuthorProfileTabs> = mutableListOf()

        sideBarNavListItems.forEach { element ->
            val href = element.attr("href")
            val tab = AuthorProfileTabs.findForPath(href)

            if (tab != null) {
                tabs += tab
            }
        }

        return tabs
    }

    override fun parseSynchronously(data: Document): StateFlow<List<AuthorProfileTabs>?> {
        TODO("Not yet implemented")
    }
}

internal class AuthorInfoParser : IDataParser<Document, AuthorInfoModel> {
    private val documentSettings = Document.OutputSettings().apply {
        this.prettyPrint(false)
    }
    override suspend fun parse(data: Document): AuthorInfoModel {
        data.outputSettings(documentSettings)
        val mb30 = data.select(".profile-container").select("section .mb-30")

        val about: String = mb30.run {
            forEach { element ->
                val header = element.select("h2").text()
                if(header.contains("О себе")) {
                    return@run element.select("div")
                        .first()
                        ?.wholeText()
                        ?.trim()
                        ?: ""
                }
            }
            return@run ""
        }

        val contacts: String = mb30.run {
            forEach { element ->
                val text = element.select("h2").text()
                if(text.contains("Контактная")) {
                    return@run element.select("div")
                        .first()
                        ?.wholeText()
                        ?.trim()
                        ?: ""
                }
            }
            return@run ""
        }

        val support: String = mb30.run {
            forEach { element ->
                val text = element.select("h2").text()
                if(text.contains("Поддержать")) {
                    return@run element.select("div")
                        .first()
                        ?.wholeText()
                        ?.trim()
                        ?: ""
                }
            }
            return@run ""
        }

        return AuthorInfoModel(
            about = about,
            contacts = contacts,
            support = support

        )
    }

    override fun parseSynchronously(data: Document): StateFlow<AuthorInfoModel?> {
        TODO("Not yet implemented")
    }
}


internal class AuthorBlogPostsParser: IDataParser<Document, List<BlogPostCardModel>> {
    override suspend fun parse(data: Document): List<BlogPostCardModel> {
        val posts = data.select(
            Evaluator.Class("user-blog-post-item content-box")
        )
        if(posts.isEmpty()) return emptyList()

        val postModels: MutableList<BlogPostCardModel> = mutableListOf()

        posts.forEach { element ->
            val (href, title) = element.select(".word-break").let {
                it.attr("href").substringBefore('#') to it.text()
            }
            val date = element.select(
                Evaluator.Class("small-text text-muted")
            ).text()

            val text = element.select(".mt-5 div")
                .first()
                ?.wholeText()
                ?.trim()
                ?: ""

            val likes = element.select("like-button")
                .attr(":like-count")
                .toIntOrNull() ?: 0
            postModels += BlogPostCardModel(
                href = href,
                title = title,
                date = date,
                text = text,
                likes = likes
            )
        }

        return postModels
    }

    override fun parseSynchronously(data: Document): StateFlow<List<BlogPostCardModel>> {
        TODO("Not yet implemented")
    }
}

class AuthorBlogPostParser: IDataParser<Document, BlogPostPageModel> {
    override suspend fun parse(data: Document): BlogPostPageModel {
        val container = data.select(".profile-container")
        val title = container.select("h1.mb-10").text().trim()
        val date = container.select("div[class=\"small-text text-muted mb-10\"]").text()
        val text = container.select("div[class=\"text-preline mb-15\"]")
            .first()
            ?.wholeText()
            ?.trim()
            ?: ""
        val likes = container.select("like-button")
            .attr(":like-count")
            .toIntOrNull() ?: 0

        return BlogPostPageModel(
            title = title,
            date = date,
            text = text,
            likes = likes
        )
    }

    override fun parseSynchronously(data: Document): StateFlow<BlogPostPageModel?> {
        TODO("Not yet implemented")
    }
}



internal class AuthorPresentsParser: IDataParser<Document, List<AuthorPresentModel>> {
    override suspend fun parse(data: Document): List<AuthorPresentModel> {
        val presents = data.select(".present-thumb")
        if(presents.isEmpty()) return emptyList()

        val presentModels: MutableList<AuthorPresentModel> = mutableListOf()

        presents.forEach { element ->
            val (user, text) = element
                .select(".present-thumb-title")
                .let {
                    val text = it.first()?.text() ?: ""
                    val a = it.select("a")

                    val user = UserModel(
                        name = a.text(),
                        href = a.attr("href"),
                    )
                    user to text
                }
            val pictureUrl = element
                .select(".present-thumb-picture")
                .attr("src")

            presentModels += AuthorPresentModel(
                pictureUrl = pictureUrl,
                text = text,
                user = user
            )
        }

        return presentModels
    }

    override fun parseSynchronously(data: Document): StateFlow<List<AuthorPresentModel>?> {
        TODO("Not yet implemented")
    }
}

internal class AuthorFanficPresentsParser: IDataParser<Document, List<AuthorFanficPresentModel>> {
    override suspend fun parse(data: Document): List<AuthorFanficPresentModel> {
        val presents = data.select(".present-thumb")
        if(presents.isEmpty()) return emptyList()

        val presentModels: MutableList<AuthorFanficPresentModel> = mutableListOf()

        presents.forEach { element ->
            val (user, text) = element
                .select(".present-thumb-title")
                .let {
                    val text = it.first()?.text() ?: ""
                    val a = it.select("a")

                    val user = UserModel(
                        name = a.text(),
                        href = a.attr("href"),
                    )
                    user to text
                }

            val pictureUrl = element
                .select(".present-thumb-picture")
                .attr("src")

            val forWork = element.select("a:eq(0)").let {
                Section(
                    name = it.text(),
                    segments = it.attr("href")
                )
            }

            presentModels += AuthorFanficPresentModel(
                pictureUrl = pictureUrl,
                text = text,
                user = user,
                forWork = forWork
            )
        }

        return presentModels
    }

    override fun parseSynchronously(data: Document): StateFlow<List<AuthorFanficPresentModel>?> {
        TODO("Not yet implemented")
    }
}

internal class AuthorCommentPresentsParser: IDataParser<Document, List<AuthorCommentPresentModel>> {
    override suspend fun parse(data: Document): List<AuthorCommentPresentModel> {
        val presents = data.select(".present-thumb")
        if(presents.isEmpty()) return emptyList()

        val presentModels: MutableList<AuthorCommentPresentModel> = mutableListOf()

        presents.forEach { element ->
            val (user, text) = element
                .select(".present-thumb-title")
                .let {
                    val text = it.first()?.text() ?: ""
                    val a = it.select("a")

                    val user = UserModel(
                        name = a.text(),
                        href = a.attr("href"),
                    )
                    user to text
                }

            val pictureUrl = element
                .select(".present-thumb-picture")
                .attr("src")

            val forWork = element.select(".present-thumb-description a").let {
                Section(
                    name = it.text().trim('«', '»'),
                    segments = it.attr("href")
                )
            }

            presentModels += AuthorCommentPresentModel(
                pictureUrl = pictureUrl,
                text = text,
                user = user,
                forWork = forWork
            )
        }

        return presentModels
    }

    override fun parseSynchronously(data: Document): StateFlow<List<AuthorCommentPresentModel>?> {
        TODO("Not yet implemented")
    }
}