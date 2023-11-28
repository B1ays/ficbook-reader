@file:Suppress("ClassName")

package ru.blays.ficbookapi.data

import kotlinx.serialization.Serializable

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
}

data class PopularSections(
    val allPopular: SectionWithQuery,
    val gen: SectionWithQuery,
    val het: SectionWithQuery,
    val slash: SectionWithQuery,
    val femslash: SectionWithQuery,
    val mixed: SectionWithQuery,
    val other: SectionWithQuery
) {
    companion object {
        fun default(): PopularSections = PopularSections(
            allPopular = SectionWithQuery(
                "Все", emptyList(), "popular-fanfics-376846"
            ),
            gen = SectionWithQuery(
                "Джен", emptyList(), "popular-fanfics-376846", "gen"
            ),
            het = SectionWithQuery(
                "Гет", emptyList(), "popular-fanfics-376846", "het"
            ),
            slash = SectionWithQuery(
                "Слэш", emptyList(), "popular-fanfics-376846", "slash-fics-783456238"
            ),
            femslash = SectionWithQuery(
                "Фемслэш", emptyList(), "popular-fanfics-376846", "femslash-fanfics-54353433"
            ),
            mixed = SectionWithQuery(
                "Смешанный", emptyList(), "popular-fanfics-376846", "mixed"
            ),
            other= SectionWithQuery(
                "Другой", emptyList(), "popular-fanfics-376846", "other"
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
                "Все", emptyList(), "fanfiction"
            ),
            animeAndManga = SectionWithQuery(
                "Аниме и манга", emptyList(), "fanfiction", "anime_and_manga"
            ),
            books = SectionWithQuery(
                "Книги", emptyList(), "fanfiction", "books"
            ),
            cartoons = SectionWithQuery(
                "Мультфильмы", emptyList(), "fanfiction", "cartoons"
            ),
            games = SectionWithQuery(
                "Игры", emptyList(), "fanfiction", "games"
            ),
            movies = SectionWithQuery(
                "Фильмы и сериалы", emptyList(), "fanfiction", "movies_and_tv_series"
            ),
            other = SectionWithQuery(
                "Другое", emptyList(), "fanfiction", "other"
            ),
            rpf = SectionWithQuery(
                "Известные люди", emptyList(), "fanfiction", "rpf"
            ),
            originals = SectionWithQuery(
                "Оригинальные", emptyList(), "fanfiction", "originals"
            ),
            comics = SectionWithQuery(
                "Комиксы", emptyList(), "fanfiction", "comics"
            ),
            musicals = SectionWithQuery(
                "Мюзиклы", emptyList(), "fanfiction", "musicals"
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
        favourites, liked, readed, follow, visited
    )
) {
    companion object {
        fun default(): UserSections = UserSections(
            favourites = SectionWithQuery(
                "Подписки на авторов", emptyList(), "home", "favourites"
            ),
            liked = SectionWithQuery(
                "Понравившиеся", emptyList(), "home", "liked_fanfics"
            ),
            readed = SectionWithQuery(
                "Прочитанные", emptyList(), "home", "readedList"
            ),
            follow = SectionWithQuery(
                "Подписки", emptyList(), "home", "followList"
            ),
            visited = SectionWithQuery(
                "Просмотренные", emptyList(), "home", "visitedList"
            )
        )
    }
}

object CollectionsTypes {
    val _personalCollections = SectionWithQuery(
        "Мои сборники",
        emptyList(),
        "home", "collections")
    val _trackedCollections = SectionWithQuery(
        "Отслеживаемые сборники",
        listOf(
            "type" to "_other"
        ),
        "home", "collections", )
}

