package ru.blays.ficbookReader.shared.data.sections

import ru.blays.ficbookReader.shared.data.dto.SectionWithQuery
import ru.blays.ficbookReader.shared.data.mappers.toStableModel


data class PopularSectionsStable(
    val allPopular: SectionWithQuery,
    val gen: SectionWithQuery,
    val het: SectionWithQuery,
    val slash: SectionWithQuery,
    val femslash: SectionWithQuery,
    val mixed: SectionWithQuery,
    val other: SectionWithQuery
)
val popularSections = with(ru.blays.ficbookapi.data.PopularSections.default()) {
    PopularSectionsStable(
        allPopular = allPopular.toStableModel(),
        gen = gen.toStableModel(),
        het = het.toStableModel(),
        slash = slash.toStableModel(),
        femslash = femslash.toStableModel(),
        mixed = mixed.toStableModel(),
        other = other.toStableModel()
    )
}

data class CategoriesSectionsStable(
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
)
val categoriesSections = with(ru.blays.ficbookapi.data.CategoriesSections.default()) {
    CategoriesSectionsStable(
        allFandoms = allFandoms.toStableModel(),
        animeAndManga = animeAndManga.toStableModel(),
        books = books.toStableModel(),
        cartoons = cartoons.toStableModel(),
        games = games.toStableModel(),
        movies = movies.toStableModel(),
        other = other.toStableModel(),
        rpf = rpf.toStableModel(),
        originals = originals.toStableModel(),
        comics = comics.toStableModel(),
        musicals = musicals.toStableModel()
    )
}

data class UserSectionsStable(
    val favourites: SectionWithQuery,
    val liked: SectionWithQuery,
    val readed: SectionWithQuery,
    val follow: SectionWithQuery,
    val visited: SectionWithQuery
)
val userSections = with(ru.blays.ficbookapi.data.UserSections.default()) {
    UserSectionsStable(
        favourites = favourites.toStableModel(),
        liked = liked.toStableModel(),
        readed = readed.toStableModel(),
        follow = follow.toStableModel(),
        visited = visited.toStableModel()
    )
}


object CollectionsTypes {
    val personalCollections: SectionWithQuery
        get() = ru.blays.ficbookapi.data.CollectionsTypes.personalCollections.toStableModel()
    val trackedCollections: SectionWithQuery
        get() = ru.blays.ficbookapi.data.CollectionsTypes.trackedCollections.toStableModel()
    
}