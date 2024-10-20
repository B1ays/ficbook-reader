package ru.blays.ficbook.api.parsers

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Evaluator
import ru.blays.ficbook.api.ATTR_HREF
import ru.blays.ficbook.api.ATTR_SRC
import ru.blays.ficbook.api.data.Section
import ru.blays.ficbook.api.dataModels.*
import ru.blays.ficbook.api.notNumberRegex


internal class AuthorMainInfoParser {
    suspend fun parse(data: Document): AuthorMainInfo {
        val profileHeader = data.select(".profile-header")

        val name = profileHeader
            .select(".user-name")
            .text()
            .trim()

        val realID: String = data.run {
            val first = select("[name=userId]").attr("value")
            if (first.isNotEmpty()) return@run first
            val second = select(".user-name-box div").getOrNull(1)?.text()
            return@run if (second?.startsWith("ID") == true) {
                return@run second.replace(
                    regex = notNumberRegex,
                    replacement = ""
                )
            } else ""
        }
        val relativeID = profileHeader
            .select(".avatar-cropper")
            .select("a")
            .attr(ATTR_HREF)
            .substringAfterLast('/')


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

        return AuthorMainInfo(
            name = name,
            realID = realID,
            relativeID = relativeID,
            avatarUrl = avatarUrl,
            profileCoverUrl = profileCoverUrl,
            subscribers = subscribers,
            subscribed = subscribed
        )
    }
}

internal class AuthorProfileTabsParser {
    suspend fun parse(data: Document): List<AuthorProfileTabs> {
        val sideBarNavListItems = data
            .select(".sidebar-nav-list")
            .select("li")

        return sideBarNavListItems.mapNotNull { element ->
            val href = element.select("a").attr(ATTR_HREF)

            when (
                val tab = AuthorProfileTabs.findForPath(href)
            ) {
                AuthorProfileTabs.BLOG -> {
                    if (element.hasCounter) {
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
}

internal class AuthorInfoParser {
    private val documentSettings = Document.OutputSettings().apply {
        this.prettyPrint(false)
    }

    suspend fun parse(data: Document): AuthorInfoModel {
        data.outputSettings(documentSettings)
        val column = data.select("[class^='col-xs-16']")
        .last()

        if(column == null) {
            return AuthorInfoModel(
                about = "",
                contacts = "",
                support = ""
            )
        }

        val sections = column.select("section > section")

        val about: String = sections.find {
            it.select("[class^='text-t1']:contains(О себе)").isNotEmpty()
        }.let {
            it?.select("div")
                ?.wholeText
                ?.trim()
                ?: ""
        }

        val contacts: String = sections.find {
            it.select("[class^='text-t1']:contains(Контактная)").isNotEmpty()
        }.let {
            it?.select("div")
                ?.wholeText
                ?.trim()
                ?: ""
        }

        val support: String = sections.find {
            it.select("[class^='text-t1']:contains(Поддержать)").isNotEmpty()
        }.let {
            it?.select("div")
                ?.wholeText
                ?.trim()
                ?: ""
        }

        return AuthorInfoModel(
            about = about,
            contacts = contacts,
            support = support
        )
    }
}

internal class AuthorBlogPostsParser {
    suspend fun parse(data: Document): List<BlogPostCardModel> {
        val posts = data.select(
            Evaluator.Class("user-blog-post-item content-box")
        )
        if (posts.isEmpty()) return emptyList()

        return posts.map { element ->
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
                .wholeText
                .trim()

            val likes = element.select("like-button")
                .attr(":like-count")
                .toIntOrNull()
                ?: 0

            BlogPostCardModel(
                id = href,
                title = title,
                date = date,
                text = text,
                likes = likes
            )
        }
    }
}

internal class AuthorBlogPostParser {
    suspend fun parse(data: Document): BlogPostPageModel {
        val container = data.select("section#content" )
        val title = container.select(".mb-10").text().trim()
        val date = container.select("div[class*=mb-10]").text()
        val text = container.select("div[class*=mb-15]")
            .wholeText
            .trim()
        val likes = container.select("like-button")
            .attr(":like-count")
            .toIntOrNull()
            ?: 0

        return BlogPostPageModel(
            title = title,
            date = date,
            text = text,
            likes = likes
        )
    }
}

internal class AuthorPresentsParser {
    suspend fun parse(data: Document): List<AuthorPresentModel> {
        val presents = data.select(".present-thumb")
        if (presents.isEmpty()) return emptyList()

        return presents.map { element ->
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

            AuthorPresentModel(
                pictureUrl = pictureUrl,
                text = text,
                user = user
            )
        }
    }
}

internal class AuthorFanficPresentsParser {
    suspend fun parse(data: Document): List<AuthorFanficPresentModel> {
        val presents = data.select(".present-thumb")
        if (presents.isEmpty()) return emptyList()

        return presents.map { element ->
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

            AuthorFanficPresentModel(
                pictureUrl = pictureUrl,
                text = text,
                user = user,
                forWork = forWork
            )
        }
    }
}

internal class AuthorCommentPresentsParser {
    suspend fun parse(data: Document): List<AuthorCommentPresentModel> {
        val presents = data.select(".present-thumb")
        if (presents.isEmpty()) return emptyList()

        return presents.map { element ->
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

            AuthorCommentPresentModel(
                pictureUrl = pictureUrl,
                text = text,
                user = user,
                forWork = forWork
            )
        }
    }
}