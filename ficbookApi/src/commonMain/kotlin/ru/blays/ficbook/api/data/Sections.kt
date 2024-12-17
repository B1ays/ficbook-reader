@file:Suppress("ClassName")

package ru.blays.ficbook.api.data

import kotlinx.serialization.Serializable

@ConsistentCopyVisibility
@Serializable
data class Section internal constructor (
    val name: String,
    val segments: String,
): java.io.Serializable {
    constructor(
        sectionName: String,
        vararg paths: String
    ): this(
        name = sectionName,
        segments = paths.joinToString("/")
    )

    companion object {
        fun Section.toSectionWithQuery(): SectionWithQuery {
            return SectionWithQuery(
                name = name,
                path = segments,
                queryParameters = emptyList()
            )
        }
    }
}

@Serializable
data class SectionWithQuery(
    val name: String,
    val path: String,
    val queryParameters: List<Pair<String, String>>?
): java.io.Serializable {
    constructor(
        name: String = "",
        queryParameters: List<Pair<String, String>>? = null,
        vararg paths: String
    ): this(
        name = name,
        path = paths.joinToString("/"),
        queryParameters = queryParameters
    )

    constructor(
        name: String,
        href: String
    ): this(
        name = name,
        queryParameters = null,
        path = href
    )

    constructor(
        href: String
    ): this(
        name = "",
        queryParameters = emptyList(),
        path = href
    )

    override fun toString(): String {
        return buildString {
            append(path)
            if(path.endsWith('/')) {
                deleteCharAt(length)
            }
            if(queryParameters?.isNotEmpty() == true) {
                append('?')
            }
            queryParameters?.forEachIndexed { index, (name, value) ->
                if(index != 0) append('&')
                append(name)
                append('=')
                append(value)
            }
        }
    }
}

data class PopularSections(
    val allPopular: SectionWithQuery,
    val gen: SectionWithQuery,
    val het: SectionWithQuery,
    val slash: SectionWithQuery,
    val femslash: SectionWithQuery,
    val article: SectionWithQuery,
    val mixed: SectionWithQuery,
    val other: SectionWithQuery
) {
    companion object {
        fun default(): PopularSections = PopularSections(
            allPopular = SectionWithQuery(
                "Все", "popular-fanfics-376846"
            ),
            gen = SectionWithQuery(
                "Джен", "popular-fanfics-376846/gen"
            ),
            het = SectionWithQuery(
                "Гет", "popular-fanfics-376846/het"
            ),
            slash = SectionWithQuery(
                "Слэш","popular-fanfics-376846/slash-fics-ngf3487tnsfb"
            ),
            femslash = SectionWithQuery(
                "Фемслэш", "popular-fanfics-376846/femslash-fanfics-kojhi9jhhmkhgi9t98"
            ),
            article = SectionWithQuery(
                "Статьи", "popular-fanfics-376846/article"
            ),
            mixed = SectionWithQuery(
                "Смешанный","popular-fanfics-376846/mixed"
            ),
            other= SectionWithQuery(
                "Другой", "popular-fanfics-376846/other"
            )
        )
    }
}

data class CategoriesSections(
    val allFandoms: SectionWithQuery,
    val animeAndManga: SectionWithQuery,
    val books: SectionWithQuery,
    val cartoons: SectionWithQuery,
    val games: SectionWithQuery,
    val movies: SectionWithQuery,
    val other: SectionWithQuery,
    val rpf: SectionWithQuery,
    val originals: SectionWithQuery,
    val comics: SectionWithQuery,
    val musicals: SectionWithQuery
) {
    companion object {
        fun default(): CategoriesSections = CategoriesSections(
            allFandoms = SectionWithQuery(
                "Все", "fanfiction"
            ),
            animeAndManga = SectionWithQuery(
                "Аниме и манга", "fanfiction/anime_and_manga"
            ),
            books = SectionWithQuery(
                "Книги", "fanfiction/books"
            ),
            cartoons = SectionWithQuery(
                "Мультфильмы", "fanfiction/cartoons"
            ),
            games = SectionWithQuery(
                "Игры", "fanfiction/games"
            ),
            movies = SectionWithQuery(
                "Фильмы и сериалы", "fanfiction/movies_and_tv_series"
            ),
            other = SectionWithQuery(
                "Другое", "fanfiction/other"
            ),
            rpf = SectionWithQuery(
                "Известные люди", "fanfiction/rpf"
            ),
            originals = SectionWithQuery(
                "Оригинальные", "fanfiction/originals"
            ),
            comics = SectionWithQuery(
                "Комиксы", "fanfiction/comics"
            ),
            musicals = SectionWithQuery(
                "Мюзиклы", "fanfiction/musicals"
            )
        )
    }
}

data class UserSections(
    val favourites: SectionWithQuery,
    val liked: SectionWithQuery,
    val readed: SectionWithQuery ,
    val follow: SectionWithQuery,
    val visited: SectionWithQuery,
    val all: Array<SectionWithQuery> = arrayOf(
        favourites,
        liked,
        readed,
        follow,
        visited
    )
) {
    companion object {
        fun default(): UserSections = UserSections(
            favourites = SectionWithQuery(
                "Подписки на авторов", "home/favourites"
            ),
            liked = SectionWithQuery(
                "Понравившиеся", "home/liked_fanfics"
            ),
            readed = SectionWithQuery(
                "Прочитанные", "home/readedList"
            ),
            follow = SectionWithQuery(
                "Подписки", "home/followList"
            ),
            visited = SectionWithQuery(
                "Просмотренные", "home/visitedList"
            )
        )
    }
}

object CollectionsTypes {
    val personalCollections = SectionWithQuery(
        "Мои сборники",
        emptyList(),
        "home", "collections")
    val trackedCollections = SectionWithQuery(
        "Отслеживаемые сборники",
        listOf(
            "type" to "other"
        ),
        "home", "collections")
}

