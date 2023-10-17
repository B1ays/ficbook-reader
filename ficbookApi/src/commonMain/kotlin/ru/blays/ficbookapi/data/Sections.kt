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
    val queryParameters: List<Pair<String, String>>
): java.io.Serializable {
    constructor(
        name: String,
        queryParameters: List<Pair<String, String>>,
        vararg paths: String
    ): this(
        name = name,
        path = paths.joinToString("/"),
        queryParameters = queryParameters
    )
}

interface SectionsCategory {
    val all: Array<Section>

    fun get(name: String): Section?
}

object PopularSections: SectionsCategory {
    val _allPopular = Section("Все", "popular-fanfics")
    val _gen = Section("Джен", "popular-fanfics", "gen")
    val _het = Section("Гет", "popular-fanfics", "het")
    val _slash = Section("Слэш", "popular-fanfics", "slash-fics-3712917")
    val _femslash = Section("Фемслэш", "popular-fanfics", "femslash-fanfics-9374932")
    val _mixed = Section("Смешанный", "popular-fanfics", "mixed")
    val _other = Section("Другой", "popular-fanfics", "other")
    
    override val all: Array<Section> = arrayOf(
        _allPopular,
        _gen,
        _het,
        _slash,
        _femslash,
        _mixed,
        _other
    )

    override fun get(name: String): Section? {
        return all.firstOrNull { it.name == name }
    }
}

object CategoriesSections: SectionsCategory {
    val _allFandoms = Section("Все", "fanfiction")
    val _animeAndManga = Section("Аниме и манга", "fanfiction", "anime_and_manga")
    val _books = Section("Книги", "fanfiction", "books")
    val _cartoons = Section("Мультфильмы", "fanfiction", "cartoons")
    val _games = Section("Игры", "fanfiction", "games")
    val _movies = Section("Фильмы и сериалы", "fanfiction", "movies_and_tv_series")
    val _other = Section("Другое", "fanfiction", "other")
    val _rpf = Section("Известные люди", "fanfiction", "rpf")
    val _originals = Section("Ориджиналы", "fanfiction", "no_fandom", "originals")
    val _comics = Section("Комиксы", "fanfiction", "comics")
    val _musicals = Section("Мюзиклы", "fanfiction", "musicals")

    override val all: Array<Section> = arrayOf(
        _allFandoms,
        _animeAndManga,
        _books,
        _cartoons,
        _games,
        _movies,
        _other,
        _rpf,
        _originals,
        _comics,
        _musicals
    )

    override fun get(name: String): Section? {
        return all.firstOrNull { it.name == name }
    }
}

object UserSections: SectionsCategory {
    val _favourites = Section("Подписки на авторов","home", "favourites")
    val _liked = Section("Понравившиеся","home", "liked_fanfics")
    val _readed = Section("Прочитанные","home", "readedList")
    val _follow = Section("Подписки","home", "followList")
    val _visited = Section("Просмотренные","home", "visitedList")
    
    override val all: Array<Section> = arrayOf(
        _favourites,
        _liked,
        _readed,
        _follow,
        _visited
    )

    override fun get(name: String): Section? {
        return all.firstOrNull { it.name == name }
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

