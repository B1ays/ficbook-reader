package ru.blays.ficbook.api.parsers

import kotlinx.coroutines.flow.StateFlow
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Evaluator
import ru.blays.ficbook.api.ATTR_HREF
import ru.blays.ficbook.api.ATTR_SRC
import ru.blays.ficbook.api.data.Section
import ru.blays.ficbook.api.dataModels.*
import ru.blays.ficbook.api.notNumberRegex


internal class AuthorMainInfoParser : IDataParser<Document, AuthorMainInfo> {
    override suspend fun parse(data: Document): AuthorMainInfo {
        val profileHeader = data.select(".profile-header")

        val name = profileHeader
            .select(".user-name")
            .text()
            .trim()

        val id: String = data.run {
            val first = select("[name=userId]").attr("value")
            if(first.isNotEmpty()) return@run first
            val second = select(".user-name-box div").getOrNull(1)?.text()
            return@run if(second?.startsWith("ID") == true) {
                return@run second.replace(
                    regex = notNumberRegex,
                    replacement = ""
                )
            } else ""
        }

        val avatarUrl = profileHeader
            .select(".avatar-cropper")
            .select("img")
            .attr(ATTR_SRC)

        val profileCoverUrl = profileHeader
            .select(".img-responsive")
            .attr(ATTR_SRC)

        val subscribers = profileHeader
            .select("div.small-text a")
            .text()
            .replace(
                regex = notNumberRegex,
                replacement = ""
            )
            .toIntOrNull()
            ?: 0

        val subscribed = profileHeader
            .select("author-to-favourites-button")
            .attr(":model-value")
            .toBooleanStrictOrNull()
            ?: false

        val availableTabs = AuthorProfileTabsParser().parse(data)

        return AuthorMainInfo(
            name = name,
            id = id,
            avatarUrl = avatarUrl,
            profileCoverUrl = profileCoverUrl,
            subscribers = subscribers,
            subscribed = subscribed,
            availableTabs = availableTabs
        )
    }

    override fun parseSynchronously(data: Document): StateFlow<AuthorMainInfo?> {
        TODO("Not yet implemented")
    }
}

internal class AuthorProfileTabsParser: IDataParser<Document, List<AuthorProfileTabs>> {
    override suspend fun parse(data: Document): List<AuthorProfileTabs> {
        val sideBarNavListItems = data
            .select(".sidebar-nav-list")
            .select("li")

        return sideBarNavListItems.mapNotNull { element ->
            val href = element.select("a").attr(ATTR_HREF)

            when(
                val tab = AuthorProfileTabs.findForPath(href)
            ) {
                AuthorProfileTabs.BLOG -> {
                    if(element.hasCounter) {
                        tab
                    } else {
                        null
                    }
                }
                else -> tab
            }
        }
    }

    private val Element.hasCounter: Boolean
        get() = allElements.hasClass("counter")

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
                val id = it.attr("href")
                    .substringBefore('#')
                    .substringAfterLast('/')
                val text = it.text()
                return@let id to text
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
                id = href,
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

internal class AuthorBlogPostParser: IDataParser<Document, BlogPostPageModel> {
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
                        href = a.attr(ATTR_HREF),
                    )
                    user to text
                }
            val pictureUrl = element
                .select(".present-thumb-picture")
                .attr(ATTR_SRC)

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
                        href = a.attr(ATTR_HREF),
                    )
                    user to text
                }

            val pictureUrl = element
                .select(".present-thumb-picture")
                .attr(ATTR_SRC)

            val forWork = element.select("a:eq(0)").let {
                Section(
                    name = it.text(),
                    segments = it.attr(ATTR_HREF)
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
                        href = a.attr(ATTR_HREF),
                    )
                    user to text
                }

            val pictureUrl = element
                .select(".present-thumb-picture")
                .attr(ATTR_SRC)

            val forWork = element.select(".present-thumb-description a").let {
                Section(
                    name = it.text().trim('«', '»'),
                    segments = it.attr(ATTR_HREF)
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